/*
package org.mbc.czo.function.product.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.product.domain.Item;
import org.mbc.czo.function.product.dto.ItemFormDto;
import org.mbc.czo.function.product.dto.ItemSearchDto;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.mbc.czo.function.product.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ItemController {
    // 관리자 상품 컨트롤러 !!!!!!!!
    private final ItemService itemService;
    private final ItemRepository itemRepository;

    // 상품 등록 관련
    @GetMapping(value = "/admin/item/new")
    public String itemForm(Model model) {
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "product/itemForm";
    }
    @PostMapping(value = "/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                          @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) {
        // @Valid : 필수 입력 등 유효성 검사하는 용도
        // BindingResult : 유효성 검사 후 에러 정보를 보관하는 객체
        // @RequestParam : 프론트에 요청하여 파라미터로 받는 것
        if (bindingResult.hasErrors()) { // 상품 등록 시 필수 값이 없다면 상품 등록 페이지로 다시 보냄
            return "product/itemForm";
        }
        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){ // 상품 등록 시 첫 번째 이미지가 없다면 에러 메시지와 함꼐 상품 등록 페이지로 전환됨
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값입니다.");
            return "product/itemForm";
        }

        try{
            itemService.saveItem(itemFormDto, itemImgFileList);
        }catch (Exception e){
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "product/itemForm";
        }
        return "redirect:/item/list"; // 상품이 정상적으로 등록되었다면 메인 목록 페이지로 이동
    }



    // 상품 수정 관련
    @GetMapping(value = "/admin/item/{itemId}") // {itemId}은 url 일부를 변수로 받아 메서드 파라미터로 넘긴다.
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model) {

        try{
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
        }catch (EntityNotFoundException e){
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto()); // 에러가 존재하니 빈 DTO를 다시 넘겨줌
            return "product/itemForm";
        }
        return "product/itemForm"; // 이건 try가 정상적으로 끝났을 때만 실행
    }

    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList, Model model){

        if (bindingResult.hasErrors()) {
            return "product/itemForm";
        }
        if(itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");
            return "product/itemForm";
        }
        try {
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch (Exception e){
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return "product/itemForm";
        }
        return "redirect:/";
    }

    // 상품 조회 관련 (관리자)
    @GetMapping(value = {"/admin/items", "/admin/items/{page}"}) // value에 상품 관리 화면 진입 시 URL에 페이지 번호가 없는 경우와 페이지 번호가 있는 경우 2가지를 매핑
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0,3); // url 경로에 페이지 번호가 있으면 해당 페이지를 조회 하도록 세팅, 없으면 0페이지를 조회하도록함
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable); // 조회 조건과 페이징 정보를 파라미터로 넘겨서 Page<item>객체를 반환 받음
        model.addAttribute("items", items); // 조회한 상품 데이터 및 페이징 정보를 뷰에 전달
        model.addAttribute("itemSearchDto", itemSearchDto); // 페이지 전환 시 기존 검색 조건을 유지한 채 이동할 수 있도록 뷰에 다시 전달
        model.addAttribute("maxPage", 5); //  상품 관리 메뉴 하단에 보여줄 페이지 번호의 최대 개수. 5로 설정했으므로 최대 5개의 이동할 페이지 번호만 보여준다.
        return "product/itemMng";
    }

    // 상품 조회 (사용자)
    @GetMapping(value = "/item/{itemId}")
    public String itemDtl(Model model, @PathVariable("itemId") Long itemId) {

        ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
        model.addAttribute("item", itemFormDto);
        return "product/itemDtl";
    }

    // 페이지 음수 방지
    @GetMapping("/items")
    public String main(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        if (page < 0) page = 0; // 음수 페이지 방어

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Item> items = itemRepository.findAll(pageRequest);

        model.addAttribute("items", items);
        return "main";
    }

    // 상품 삭제
    @DeleteMapping("/admin/items")
    public ResponseEntity<String> deleteItem(@RequestParam List<Long> itemIds) {
        try{
            // 서비스를 통해 실제 삭제 로직 호출
            itemService.deleteItem(itemIds);
            return ResponseEntity.ok("상품 삭제 완료");
        } catch (EntityNotFoundException e) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("삭제 할 상품이 없습니다.");
        } catch (Exception e) {
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 삭제 중 오류 발생: " + e.getMessage());
        }
    }




    */
/* 여기에서부터 혜진 추가 *//*



}
*/



package org.mbc.czo.function.product.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.product.domain.Item;
import org.mbc.czo.function.product.dto.ItemFormDto;
import org.mbc.czo.function.product.dto.ItemSearchDto;
import org.mbc.czo.function.product.dto.MainItemDto;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.mbc.czo.function.product.service.ItemService;
import org.mbc.czo.function.review.domain.Review;
import org.mbc.czo.function.review.dto.ReviewResponseDTO;
import org.mbc.czo.function.review.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Log4j2
@Controller
@RequiredArgsConstructor
public class ItemController {
    // 관리자 상품 컨트롤러 !!!!!!!!
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    // 서비스 주입
    private final ReviewService reviewService;



    // 권한 체크 헬퍼 메서드
    private boolean isSuperAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("SUPER_ADMIN"));
    }


    // 상품 등록 관련
    @GetMapping(value = "/admin/item/new")
    public String itemForm(Model model) {
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "product/itemForm";
    }


    @PostMapping(value = "/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model
            /*@RequestParam("itemImgFile") List<MultipartFile> itemImgFileList*/) {
        // @Valid : 필수 입력 등 유효성 검사하는 용도
        // BindingResult : 유효성 검사 후 에러 정보를 보관하는 객체
        // @RequestParam : 프론트에 요청하여 파라미터로 받는 것
        if (bindingResult.hasErrors()) { // 상품 등록 시 필수 값이 없다면 상품 등록 페이지로 다시 보냄
            return "product/itemForm";
        }
      /*  if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){ // 상품 등록 시 첫 번째 이미지가 없다면 에러 메시지와 함께 상품 등록 페이지로 전환됨
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값입니다.");
            return "product/itemForm";
        }*/

        try{
            itemService.saveItem(itemFormDto);
        }catch (Exception e){
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "product/itemForm";
        }
        return "redirect:/products"; // 상품이 정상적으로 등록되었다면 상품 목록 페이지로 리다이렉트
    }


    /*@GetMapping("/products")
    public String itemList(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        // itemSearchDto null 체크 및 기본값 설정
        if (itemSearchDto == null) {
            itemSearchDto = new ItemSearchDto();
        }

        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("items", items);
        model.addAttribute("maxPage", 5);

        return "product/itemList"; // 올바른 템플릿 경로
    }*/

    /*// 상품 목록 페이지 (사용자)
    @GetMapping("/products")
    public String itemList(ItemSearchDto itemSearchDto,
                           Optional<Integer> page,
                           Model model) {

        if (itemSearchDto == null) {
            itemSearchDto = new ItemSearchDto();
        }

        Pageable pageable;
        // 정렬 기준이 있으면 Sort 추가
        if ("priceAsc".equals(itemSearchDto.getSortBy())) {
            pageable = PageRequest.of(page.orElse(0), 6, Sort.by("price").ascending());
        } else if ("priceDesc".equals(itemSearchDto.getSortBy())) {
            pageable = PageRequest.of(page.orElse(0), 6, Sort.by("price").descending());
        } else {
            pageable = PageRequest.of(page.orElse(0), 6);
        }

        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("items", items);
        model.addAttribute("maxPage", 5);

        return "product/itemList";
    }*/

    // 상품 목록 페이지 (사용자용) + 정렬 기능
    @GetMapping("/products")
    public String itemList(ItemSearchDto itemSearchDto,
                           Optional<Integer> page,
                           Model model) {

        if (itemSearchDto == null) {
            itemSearchDto = new ItemSearchDto();
        }

        // 정렬 옵션이 없으면 최신순(id 내림차순)으로 기본 정렬
        Sort sort = Sort.by("id").descending();

        String sortBy = itemSearchDto.getSortBy();
        if ("priceAsc".equals(sortBy)) {
            sort = Sort.by("price").ascending();    // 낮은 가격순
        } else if ("priceDesc".equals(sortBy)) {
            sort = Sort.by("price").descending();   // 높은 가격순
        } else if ("likesDesc".equals(sortBy)) {
            // [추가된 기능] 좋아요 많은순
            // 참고: 'Item' 엔티티에 'likes' 필드가 있어야 합니다.
            sort = Sort.by("likes").descending();
        } else if ("viewsAsc".equals(sortBy)) {
            // [추가된 기능] 조회수 낮은순
            // 참고: 'Item' 엔티티에 'views' 필드가 있어야 합니다.
            sort = Sort.by("views").ascending();
        } else if ("viewsDesc".equals(sortBy)) {
            // [추가된 기능] 조회수 높은순
            // 참고: 'Item' 엔티티에 'views' 필드가 있어야 합니다.
            sort = Sort.by("views").descending();
        }

        Pageable pageable = PageRequest.of(page.orElse(0), 6, sort);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("items", items);
        model.addAttribute("maxPage", 5);

        return "product/itemList";
    }



    // 상품 수정 관련
    @GetMapping(value = "/admin/item/{itemId}") // {itemId}은 url 일부를 변수로 받아 메서드 파라미터로 넘긴다.
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model) {
        try{
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            log.info("itemDtl" + itemFormDto.toString());
            model.addAttribute("itemFormDto", itemFormDto);
        }catch (EntityNotFoundException e){
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto()); // 에러가 존재하니 빈 DTO를 다시 넘겨줌
            return "product/itemForm";
        }
        return "product/itemForm"; // 이건 try가 정상적으로 끝났을 때만 실행
    }

    /*@PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model){
        log.info("itemUpdate0");
        if (bindingResult.hasErrors()) {

            return "product/itemForm";
        }
        if(itemFormDto.getRepImgUrl() == null){
            model.addAttribute("errorMessage", "대표 이미지는 필수 입력 값 입니다.");

            return "product/itemForm";
        }
        try {

            itemService.updateItem(itemFormDto);
        } catch (Exception e){
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");

            return "product/itemForm";
        }

        return "redirect:/products"; // 상품 수정 후 상품 목록으로 리다이렉트
    }*/

    // 상품 수정 관련
    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@PathVariable("itemId") Long itemId,@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model) {
        log.info("itemUpdate0");
        if (bindingResult.hasErrors()) {
            return "product/itemForm";
        }

        // [수정] 대표 이미지 URL이 없으면 에러
        if(itemFormDto.getRepImgUrl() == null || itemFormDto.getRepImgUrl().isEmpty()) {
            model.addAttribute("errorMessage", "대표 이미지는 필수 입력 값입니다.");
            return "product/itemForm";
        }

        try {
            // [수정] ItemFormDto만 파라미터로 넘겨서 서비스에서 이미지 처리하도록 변경
            itemService.updateItem(itemFormDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다. 원인: " + e.getMessage());
            return "product/itemForm";
        }

        //return "redirect:/products";
        // 💡 수정: 상품 상세보기로 리다이렉트
        return "redirect:/item/" + itemId;
    }


    // 상품 조회 관련 (관리자)
    @GetMapping(value = {"/admin/items", "/admin/items/{page}"}) // value에 상품 관리 화면 진입 시 URL에 페이지 번호가 없는 경우와 페이지 번호가 있는 경우 2가지를 매핑
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0,3); // url 경로에 페이지 번호가 있으면 해당 페이지를 조회 하도록 세팅, 없으면 0페이지를 조회하도록함
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable); // 조회 조건과 페이징 정보를 파라미터로 넘겨서 Page<item>객체를 반환 받음
        model.addAttribute("items", items); // 조회한 상품 데이터 및 페이징 정보를 뷰에 전달
        model.addAttribute("itemSearchDto", itemSearchDto); // 페이지 전환 시 기존 검색 조건을 유지한 채 이동할 수 있도록 뷰에 다시 전달
        model.addAttribute("maxPage", 5); //  상품 관리 메뉴 하단에 보여줄 페이지 번호의 최대 개수. 5로 설정했으므로 최대 5개의 이동할 페이지 번호만 보여준다.
        return "product/itemMng";
    }

    // 상품 상세 조회 (사용자)
    @GetMapping(value = "/item/{itemId}")
    public String itemDetail(Model model, @PathVariable("itemId") Long itemId) {
        ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
        model.addAttribute("item", itemFormDto);

        List<ReviewResponseDTO> reviews = reviewService.getReviewListByItem(itemId);
        model.addAttribute("reviews", reviews);
        return "product/itemDtl";
    }

    // 페이지 음수 방지 (기존 메인 페이지용)
    @GetMapping("/items")
    public String main(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        if (page < 0) page = 0; // 음수 페이지 방어

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Item> items = itemRepository.findAll(pageRequest);

        model.addAttribute("items", items);
        return "main";
    }

    // 상품 삭제 + 추가 부분
    @DeleteMapping("/admin/items")
    @ResponseBody // 프론트 Ajax로 처리하도록 수정
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> deleteItem(@RequestParam List<Long> itemIds, Authentication authentication) {
        log.info("===== 상품 삭제 요청: itemIds={} =====", itemIds);

        // 권한 체크
//        if (!isSuperAdmin(authentication)) {
//            log.warn("권한 없는 사용자의 상품 삭제 시도");
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한이 없습니다.");
//        }

        try{
            // 서비스를 통해 실제 삭제 로직 호출
            itemService.deleteItem(itemIds);
            return ResponseEntity.ok("상품 삭제 완료");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("삭제 할 상품이 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    // ▼ [추가] 좋아요 요청을 처리할 POST 메소드
    @PostMapping(value = "/item/{itemId}/like")
    @ResponseBody
    public ResponseEntity<String> addLike(@PathVariable("itemId") Long itemId) {
        try {
            itemService.addLike(itemId);
            return new ResponseEntity<>("좋아요 처리 완료", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("좋아요 처리 중 오류 발생", HttpStatus.BAD_REQUEST);
        }
    }

    // 상품 등록 권한 체크 API
    @GetMapping("/admin/item/new/check")
    @ResponseBody
    public ResponseEntity<Void> checkItemRegisterAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("SUPER_ADMIN"));

        if (!isSuperAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().build();
    }

    // 💡 상품 수정 권한 체크 API 추가
    @GetMapping("/admin/item/{itemId}/check")
    @ResponseBody
    public ResponseEntity<Void> checkItemModifyAuth(@PathVariable("itemId") Long itemId,
                                                    Authentication authentication) {
        log.info("===== 상품 수정 권한 체크: itemId={} =====", itemId);

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("SUPER_ADMIN"));

        if (!isSuperAdmin) {
            log.warn("권한 없음");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("권한 확인 성공");
        return ResponseEntity.ok().build();
    }




}

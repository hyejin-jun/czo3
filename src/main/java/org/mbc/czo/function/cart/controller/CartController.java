package org.mbc.czo.function.cart.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiMember.dto.info.MemberInfoRes;
import org.mbc.czo.function.cart.domain.Cart;
import org.mbc.czo.function.cart.dto.CartItemDTO;
import org.mbc.czo.function.cart.dto.CartSumDTO;
import org.mbc.czo.function.cart.dto.CartDetailDTO;
import org.mbc.czo.function.cart.repository.CartItemRepository;
import org.mbc.czo.function.cart.repository.CartRepository;
import org.mbc.czo.function.cart.service.CartService;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final MemberJpaRepository memberJpaRepository;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;


    @GetMapping("/api/cart")
    public ResponseEntity<?> getCartData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String memberId = authentication.getName();
        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByMemberMid(member.getMid())
                .orElseGet(() -> cartRepository.save(Cart.createCart(member)));

        List<CartDetailDTO> cartItems = cartItemRepository.findCartDetailDTOList(cart.getId());

        Map<String, Object> response = Map.of(
                "cartItems", cartItems,
                "member", MemberInfoRes.createMemberInfoRes(member) // ✅ DTO 사용
        );

        log.info("CartDetailDTO: " + cartItems.toString());

        return ResponseEntity.ok(response);
    }


    /** ============================
     * 장바구니에 상품 담기 (로그인한 회원만)
     * POST /cart
     * ============================ */
    @PostMapping
    public @ResponseBody ResponseEntity<?> addToCart(
            @RequestBody @Valid CartItemDTO cartItemDTO,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        // 로그인한 회원만 사용
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        String memberId = authentication.getName();

        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원이 없습니다."));

        Long cartItemId = cartService.addCart(cartItemDTO, member);

        return new ResponseEntity<>(cartItemId, HttpStatus.OK);
    }


    /** ============================
     * 장바구니 목록 보기
     * GET /cart
     * ============================ */
    @GetMapping
    public String showCart(Model model, Authentication authentication) {

/*이현우 수정 getCartData에서 데이터 받아옴*/

 /*       log.info("showCart" + authentication.getName());
        log.info("showCart" + authentication.toString());
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:members/login";
        }

        String memberId = authentication.getName();

        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByMemberMemail(member.getMemail())
                .orElseGet(() -> cartRepository.save(Cart.createCart(member)));

        List<CartDetailDTO> cartItems = cartItemRepository.findCartDetailDTOList(cart.getId());

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("member", member);*/

        return "cart/cartList";
    }

    // =============================
    // 장바구니 수량 수정 (AJAX)
    // =============================
    @PatchMapping("/{cartItemId}")
    public ResponseEntity<?> updateCartItemCount(@PathVariable Long cartItemId,
                                                 @RequestBody Map<String, Integer> requestData,
                                                 Authentication authentication) {
        int count = requestData.get("count");
        cartService.updateCartItemCount(cartItemId, count, authentication.getName());
        return ResponseEntity.ok().build();
    }

    /** ============================
     * 장바구니 항목 삭제 (AJAX로 DB 삭제 적용)
     * DELETE /cart/{cartItemId}/delete
     * ============================ */
    @DeleteMapping("/{cartItemId}/delete") // 기존 @PostMapping -> DELETE로 변경
    public ResponseEntity<?> removeCartItem(@PathVariable Long cartItemId, Authentication authentication) {
        cartService.removeCartItem(cartItemId, authentication.getName()); // DB에서 삭제
        return ResponseEntity.ok().build(); // AJAX용 응답
    }

    /** ============================
     * 장바구니 전체 비우기
     * POST /cart/clear
     * ============================ */
    @PostMapping("/clear")
    public String clearCartByPost(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return "redirect:/cart";
    }

    /** ============================
     * 장바구니 요약 정보 (총 수량, 총 가격)
     * GET /cart/sum
     * ============================ */
    @GetMapping("/sum")
    public @ResponseBody ResponseEntity<CartSumDTO> getCartSum(Authentication authentication) {
        CartSumDTO summary = cartService.getCartSum(authentication.getName());
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }
}

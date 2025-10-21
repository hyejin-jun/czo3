import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SimpleTest {
    @Id
    private Long id;
    private String name;
}

package rs.banka4.stock_service.domain.listing.db;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import rs.banka4.stock_service.domain.exchanges.db.Exchange;
import rs.banka4.stock_service.domain.security.Security;

@Entity
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Table(name = "listings")
public class Listing {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne(optional = false)
    private Security security;

    @ManyToOne(optional = false)
    private Exchange exchange;

    @Column(nullable = false)
    private OffsetDateTime lastRefresh;

    @Column(nullable = false)
    private BigDecimal bid;

    @Column(nullable = false)
    private BigDecimal ask;

    @Column(nullable = false)
    private int contractSize;

    @Builder.Default
    private boolean active = true;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass =
            o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer()
                    .getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass =
            this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer()
                    .getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Listing listing = (Listing) o;
        return getId() != null && Objects.equals(getId(), listing.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode()
            : getClass().hashCode();
    }
}

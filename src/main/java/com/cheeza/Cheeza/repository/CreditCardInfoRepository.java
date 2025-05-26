
package com.cheeza.Cheeza.repository;

import com.cheeza.Cheeza.model.CreditCardInfo;
import com.cheeza.Cheeza.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreditCardInfoRepository extends JpaRepository<CreditCardInfo, Long> {
    List<CreditCardInfo> findByUser(User user);
}

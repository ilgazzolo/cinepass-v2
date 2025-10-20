package com.api.boleteria.repository;

import com.api.boleteria.log.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPaymentLogRepository extends JpaRepository<PaymentLog, Long> {
}

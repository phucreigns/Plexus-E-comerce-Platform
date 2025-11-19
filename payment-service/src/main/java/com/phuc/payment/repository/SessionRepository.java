package com.phuc.payment.repository;

import com.phuc.payment.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String> {}
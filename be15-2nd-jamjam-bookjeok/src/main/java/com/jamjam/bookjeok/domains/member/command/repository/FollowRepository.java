package com.jamjam.bookjeok.domains.member.command.repository;

import com.jamjam.bookjeok.domains.member.command.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow,Long> {

    boolean existsByFollowingUidAndFollowerUid(Long followingUid, Long followerUid);

    Optional<Follow> findByFollowingUidAndFollowerUid(Long followingUid, Long followerUid);

}

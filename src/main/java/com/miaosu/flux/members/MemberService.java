package com.miaosu.flux.members;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.bills.Balance;
import com.miaosu.flux.bills.BalanceRepository;
import com.miaosu.flux.system.users.UserService;
import com.miaosu.flux.util.DESUtil;
import com.miaosu.flux.util.UUIDGen;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * Member Service
 * Created by angus on 15/6/19.
 */
@Service
@Transactional(readOnly = true, timeout = 10)
public class MemberService {
    private static Logger logger = LoggerFactory.getLogger(MemberService.class);

    private static final String DAFAULT_PASSWORD_SUFFIX = "1q2w(I";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private Cache memberCache;

    public Member get(String userName) {
        Member member =null;
        try {
            member = memberCache.get(userName, Member.class);
        } catch (Exception ex) {
            logger.info("Get member from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
        }

        if(member == null) {
            synchronized (this){
                //  再次尝试从缓存获取
                try {
                    member = memberCache.get(userName, Member.class);
                    if (member != null) {
                        return member;
                    }
                } catch (Exception ex) {
                    logger.info("Get member from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
                }

                // 从数据库获取
                member = memberRepository.findOne(userName);

                if(member != null) {
                    // 不为空时，存入缓存
                    try {
                        memberCache.put(userName, member);
                    } catch (Exception ex) {
                        logger.info("Put member to cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
                    }
                }
            }
        }
        return member;
    }

    public Page<Member> find(String text, Pageable pageable) {
        if (text == null) {
            text = "";
        }

        return memberRepository.findByCondition(text, pageable);
    }

    @Modifying
    @Transactional
    public Member create(Member member) {
        String userName = member.getUsername();
        if (userService.userExists(userName)) {
            throw new ServiceException(ResultCode.DATA_EXISTS);
        }
        userService.create(userName, userName + DAFAULT_PASSWORD_SUFFIX, true, AuthorityUtils.createAuthorityList("ROLE_USER"));
        balanceRepository.save(new Balance(userName));
        return memberRepository.saveAndFlush(member);
    }

    @Modifying
    @Transactional
    public Member update(Member member) {
        String userName = member.getUsername();
        if (!userService.userExists(userName)) {
            throw new ServiceException(ResultCode.DATA_NOT_EXISTS);
        }

        try {
            memberCache.evict(userName);
        } catch (Exception ex) {
            logger.info("Remove member from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
        }

        return memberRepository.saveAndFlush(member);
    }

    @Modifying
    @Transactional
    public String resetToken(String userName) {
        if (!userService.userExists(userName)) {
            throw new ServiceException(ResultCode.DATA_NOT_EXISTS);
        }
        String token = generateToken();
        String encryptToken = DESUtil.encryptToString(token, userName);
        if (!StringUtils.hasText(encryptToken)) {
            logger.warn("秘钥加密失败；token:{}, userName:{}", token, userName);
            throw new ServiceException(ResultCode.FAILED);
        }
        memberRepository.resetToken(userName, encryptToken);
        try {
            memberCache.evict(userName);
        } catch (Exception ex) {
            logger.info("Remove member from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
        }
        return token ;
    }

    @Modifying
    @Transactional
    public void remove(String... userNames) {
        for (String userName : userNames) {
            try {
                memberCache.evict(userName);
            } catch (Exception ex) {
                logger.info("Remove member from cache failed. userName:{}, exMsg:{}", userName, ex.getMessage());
            }
            memberRepository.delete(userName);
            userService.delete(userName);
            balanceRepository.delete(userName);
        }
    }

    @Modifying
    @Transactional
    public void updateLastLoginInfo(String userName, Date lastLoginTime, String lastLoginIp) {
        memberRepository.updateLastLoginInfo(userName, lastLoginTime, lastLoginIp);
    }

    public String generateToken() {
        return Base64.encodeBase64String(UUIDGen.systemUuid().getBytes());
    }
}

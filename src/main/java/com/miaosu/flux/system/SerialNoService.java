package com.miaosu.flux.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by angus on 15/9/29.
 */
@Service
@Transactional(timeout = 10)
public class SerialNoService {
    @Autowired
    private SerialNoRepository serialNoRepository;

    public Long curVal(String seqName){
        return serialNoRepository.curVal(seqName);
    }

    public Long nextVal(String seqName){
        return serialNoRepository.nextVal(seqName);
    }
}

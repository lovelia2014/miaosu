package com.miaosu.flux.products;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 商品Service
 * Created by angus on 15/10/2.
 */
@Service
@Transactional(readOnly = true, timeout = 10)
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Page<Product> find(String text, Pageable pageable) {
        if (text == null) {
            text = "";
        }
        return productRepository.findByCondition(text, pageable);
    }

    public List<Product> listAll(){
        return productRepository.findAll();
    }

    public Product get(String id) {
        return productRepository.findOne(id);
    }


    @Modifying
    @Transactional
    public void remove(String... ids) {
        for (String id : ids) {
            productRepository.delete(id);
        }
    }

    @Modifying
    @Transactional
    public Product create(Product product) {
        String id = product.getId();
        if (productRepository.exists(id)) {
            throw new ServiceException(ResultCode.DATA_EXISTS);
        }

        return productRepository.saveAndFlush(product);
    }

    @Modifying
    @Transactional
    public Product update(Product product) {
        String id = product.getId();
        if (!productRepository.exists(id)) {
            throw new ServiceException(ResultCode.DATA_NOT_EXISTS);
        }

        return productRepository.saveAndFlush(product);
    }

    @Modifying
    @Transactional
    public void disable(String... ids) {
        for (String id : ids) {
            productRepository.disable(id);
        }
    }

    @Modifying
    @Transactional
    public void enable(String... ids) {
        for (String id : ids) {
            productRepository.enable(id);
        }
    }
}

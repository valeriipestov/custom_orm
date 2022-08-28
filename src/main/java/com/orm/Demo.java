package com.orm;

import java.math.BigDecimal;

import org.postgresql.ds.PGSimpleDataSource;

import com.orm.domain.Product;
import com.orm.session.SessionFactory;

public class Demo {

    public static void main(String[] args) {
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5432/postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");

        var sf = new SessionFactory(dataSource);
        var session = sf.createSession();
        var prod_1 = session.find(Product.class, 1);
        prod_1.setName("test");
        prod_1.setPrice(new BigDecimal(111));
        var prod_2 = session.find(Product.class, 1);

        var prod = new Product();
        prod.setId(11);
        prod.setName("newName");
        prod.setPrice(new BigDecimal(5555));
        session.persist(prod);

        session.remove(prod);

        var prod_2 = new Product();
        prod_2.setId(111);
        prod_2.setName("newName_111");
        prod_2.setPrice(new BigDecimal(123));
        session.persist(prod_2);

        session.close();
//        System.out.println(prod_1 == prod_2);
    }


}

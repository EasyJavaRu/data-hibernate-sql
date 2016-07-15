package ru.easyjava.data.hibernate.entity;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.junit.Before;
import org.junit.Test;
import ru.easyjava.data.hibernate.dto.CompanyNameDTO;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Closeable;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class QueryTest {
    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        Passport p = new Passport();
        p.setSeries("AS");
        p.setNo("123456");
        p.setIssueDate(LocalDate.now());
        p.setValidity(Period.ofYears(20));

        Address a = new Address();
        a.setCity("Kickapoo");
        a.setStreet("Main street");
        a.setBuilding("1");

        Person person = new Person();
        person.setFirstName("Test");
        person.setLastName("Testoff");
        person.setDob(LocalDate.now());
        person.setPrimaryAddress(a);
        person.setPassport(p);

        Company c = new Company();
        c.setName("Acme Ltd");

        p.setOwner(person);
        person.setWorkingPlaces(Collections.singletonList(c));

        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        try {
            sessionFactory = new MetadataSources(registry)
                    .buildMetadata()
                    .buildSessionFactory();

        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }

        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.merge(person);
        session.getTransaction().commit();
        session.close();
    }

    @Test
    public void testGreeter() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        System.out.println("No persons: "+
                session.createSQLQuery("select count(id) as c from Person")
                        .addScalar("c", IntegerType.INSTANCE)
                        .uniqueResult());

        List<Object[]> passportIds = session.createSQLQuery("select id, passport_id from Person")
                .list();
        passportIds.forEach(p -> System.out.println("User id: "+p[0]+" Passport id: "+p[1]));

        session.createSQLQuery("select p.* from Passport as p, Person as pe where p.id=pe.passport_id and  pe.lastName='Testoff'")
                .addEntity(Passport.class)
                .list()
                .forEach(System.out::println);

        session.createSQLQuery("select * from Person as p join Passport as pa on p.passport_id=pa.id and p.lastName = :name")
                .addEntity("p",Person.class)
                .addJoin("pa", "p.passport")
                .setResultTransformer( Criteria.ROOT_ENTITY )
                .setString("name", "Testoff")
                .list()
                .forEach(System.out::println);

        session.createSQLQuery("select name as \"name\"from Company")
                .setResultTransformer(Transformers.aliasToBean(CompanyNameDTO.class))
                .list()
                .forEach(System.out::println);

        session.getNamedQuery("findCompanyWithName")
                .setParameter("name", "Ac%")
                .list()
                .forEach(System.out::println);

        session.getNamedQuery("findCompanyNameOnly")
                .list()
                .forEach(System.out::println);
        session.getTransaction().commit();
        session.close();
    }
}

package ru.easyjava.data.hibernate.entity;


import lombok.Getter;
import lombok.Setter;
import ru.easyjava.data.hibernate.dto.CompanyNameDTO;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("PMD")
@Entity
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "findCompanyWithName",
                query = "select * from Company where name like :name",
                resultClass = Company.class
        ),
        @NamedNativeQuery(
                name = "findCompanyNameOnly",
                query = "select name from Company",
                resultSetMapping = "company_name_dto"
        )

})
@SqlResultSetMapping(
        name = "company_name_dto",
        classes = @ConstructorResult(
                targetClass = CompanyNameDTO.class,
                columns = {
                        @ColumnResult(name="name")
                }
        )
)
public class Company extends AbstractIdentifiableObject {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    @ManyToMany(mappedBy = "workingPlaces")
    private Collection<Person> workers;

    @Override
    public String toString() {
        return "Company{" +
                "name='" + name + '\'' +
                ", workers=" + workers
                .stream()
                .map(Person::getFirstName)
                .collect(Collectors.joining(","))
                + '}';
    }
}

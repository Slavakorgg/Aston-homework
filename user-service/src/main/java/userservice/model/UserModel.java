package userservice.model;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import userservice.entity.User;

import java.time.LocalDateTime;
import java.util.Objects;

@Relation(collectionRelation = "users", itemRelation = "user")
public class UserModel extends RepresentationModel<UserModel> {

    private Long id;
    private String name;
    private String email;
    private Integer age;
    private LocalDateTime createdAt;

    public static UserModel from(User user) {
        UserModel model = new UserModel();
        model.setId(user.getId());
        model.setName(user.getName());
        model.setEmail(user.getEmail());
        model.setAge(user.getAge());
        model.setCreatedAt(user.getCreatedAt());
        return model;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        UserModel userModel = (UserModel) o;
        return Objects.equals(id, userModel.id)
                && Objects.equals(name, userModel.name)
                && Objects.equals(email, userModel.email)
                && Objects.equals(age, userModel.age)
                && Objects.equals(createdAt, userModel.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name, email, age, createdAt);
    }
}

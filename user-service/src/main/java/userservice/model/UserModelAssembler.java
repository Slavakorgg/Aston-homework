package userservice.model;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import userservice.controller.UserController;
import userservice.entity.User;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, UserModel> {

    @Override
    public UserModel toModel(User entity) {
        UserModel model = UserModel.from(entity);
        model.add(
                linkTo(methodOn(UserController.class).getUserById(entity.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"),
                linkTo(methodOn(UserController.class).updateUser(entity.getId(), null)).withRel("update"),
                linkTo(methodOn(UserController.class).deleteUser(entity.getId())).withRel("delete")
        );
        return model;
    }

    @Override
    public CollectionModel<UserModel> toCollectionModel(Iterable<? extends User> entities) {
        List<UserModel> models = new ArrayList<>();
        for (User entity : entities) {
            models.add(toModel(entity));
        }
        return CollectionModel.of(models,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
    }
}

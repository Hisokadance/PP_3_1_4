package ru.igor.PP_3_1_3.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.igor.PP_3_1_3.model.Role;
import ru.igor.PP_3_1_3.model.User;
import ru.igor.PP_3_1_3.services.RoleServices;
import ru.igor.PP_3_1_3.services.UserServices;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserServices userService;
    private final RoleServices roleService;

    @Autowired
    public AdminController(UserServices userService, RoleServices roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping()
    public String getUsers(Principal principal, Model model) {
        model.addAttribute("users", userService.findAll());

        addAttributesToMainPage(model, principal);

        return "/admin/all-users";
    }

    @PostMapping()
    public String createUser(@ModelAttribute("newUser") @Valid User newUser,
                             BindingResult bindingResult, Model model, Principal principal) {
        User userByEmail = userService.findByEmail(newUser.getEmail());
        if (userByEmail != null) {
            bindingResult.rejectValue("email", "error.email",
                    "This email is already in use");

        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("hasErrors", true);
            addAttributesToMainPage(model, principal);
            return "/admin/all-users";
        }

        this.userService.save(newUser);
        return "redirect:/admin";
    }

    @GetMapping("/update")
    public String showFormForUpdate(@RequestParam("id") long id,
                                    Model model) {
        User user = userService.readUser(id);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("listRoles", roleService.getAllRoles());
            return "/admin/update-user";
        } else {
            return "redirect:/admin";
        }
    }

    @PatchMapping("/edit")
    public String editUser(@ModelAttribute("updatingUser") @Valid User updatingUser,
                           BindingResult bindingResult, Model model, Principal principal) {
        User userByEmail = userService.findByEmail(updatingUser.getEmail());
        if (userByEmail != null && (userByEmail.getId() != (updatingUser.getId()))) {
            bindingResult.rejectValue("email", "error.email",
                    "This email is already in use");
        }

        if (bindingResult.hasErrors()) {
            addAttributesToMainPage(model, principal);
            model.addAttribute("editUserError", true);
            return "/admin/all-users";
        }

        userService.save(updatingUser);
        return "redirect:/admin";
    }

    @DeleteMapping("/delete")
    public String deleteUser(@ModelAttribute("deletingUser") User user) {
        long id = user.getId();
        if (userService.readUser(id) != null) {
            userService.delete(id);
        }
        return "redirect:/admin";
    }

    private void addAttributesToMainPage(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        if (user == null) {
            user = new User();
        }

        List<String> roles;
        Set<Role> userRoles = user.getRoles();
        if (userRoles != null) {
            roles = userRoles.stream()
                    .map(Role::getName)
                    .map(role -> role.split("_")[1])
                    .toList();
        } else {
            roles = Collections.emptyList(); // возвращает пустой список в случае, если roles == null
        }

        model.addAttribute("authUser", user);
        model.addAttribute("userRoles", roles);

        if (!model.containsAttribute("updatingUser")) {
            model.addAttribute("updatingUser", new User());
        }

        if (!model.containsAttribute("newUser")) {
            model.addAttribute("newUser", new User());
        }

        if (!model.containsAttribute("deletingUser")) {
            model.addAttribute("deletingUser", new User());
        }

        model.addAttribute("listRoles", roleService.getAllRoles());
        model.addAttribute("authUser", user);
    }
}

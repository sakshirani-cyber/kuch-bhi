package com.authentication.AuthProject.controller;

import com.authentication.AuthProject.dto.request.ChangePasswordRequest;
import com.authentication.AuthProject.dto.request.UpdateProfileRequest;
import com.authentication.AuthProject.dto.response.UserResponse;
import com.authentication.AuthProject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    //id should be unique and unpredictable
    //use try catch
    //http codes to be used


//    @GetMapping("/profile/{id}")
//    public String profile(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
//        var user = service.getUser(id);
//
//        if (user.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "User not found.");
//            return "redirect:/login";
//        }
//
//        model.addAttribute("user", user.get());
//        return "profile";
//    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Long id) {

        UserResponse response = service.getUser(id);

        return ResponseEntity.ok(response);
    }


//    @GetMapping("/edit-profile/{id}")
//    public String editProfilePage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
//        var user = service.getUser(id);
//
//        if (user.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "User not found.");
//            return "redirect:/login";
//        }
//
//        model.addAttribute("user", user.get());
//        return "edit-profile";
//    }

//
//    @PutMapping("/edit-profile/{id}")
//    public String updateProfile(@PathVariable Long id,
//                                @ModelAttribute UpdateProfileRequest request,
//                                RedirectAttributes redirectAttributes) {
//        var error = service.updateUser(id, request);
//
//        if (error.isPresent()) {
//            redirectAttributes.addFlashAttribute("error", error.get());
//            return "redirect:/edit-profile/" + id;
//        }
//
//        redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
//        return "redirect:/profile/" + id;
//    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = service.updateUser(id, request);

        return ResponseEntity.ok(response);
    }
//    @GetMapping("/change-password/{id}")
//    public String changePasswordPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
//        var user = service.getUser(id);
//
//        if (user.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "User not found.");
//            return "redirect:/login";
//        }
//
//        model.addAttribute("userId", id);
//        return "change-password";
//    }
//
//
//    @PutMapping("/change-password/{id}")
//    public String changePassword(@PathVariable Long id,
//                                   @ModelAttribute ChangePasswordRequest request,
//                                   RedirectAttributes redirectAttributes) {
//        var error = service.changePassword(id, request);
//
//        if (error.isPresent()) {
//            redirectAttributes.addFlashAttribute("error", error.get());
//            return "redirect:/change-password/" + id;
//        }
//
//        redirectAttributes.addFlashAttribute("message", "Password updated successfully.");
//        return "redirect:/home/" + id;
//    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody  ChangePasswordRequest request) {

        service.changePassword(id, request);

        return ResponseEntity.noContent().build();
    }
}

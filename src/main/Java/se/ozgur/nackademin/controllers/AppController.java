package se.ozgur.nackademin.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.ozgur.nackademin.model.FileBucket;
import se.ozgur.nackademin.model.User;
import se.ozgur.nackademin.model.UserDocument;
import se.ozgur.nackademin.service.UserDocumentService;
import se.ozgur.nackademin.service.UserService;
import se.ozgur.nackademin.util.FileValidator;


@Controller
@RequestMapping("/")
public class AppController {

    @Autowired
    UserService userService;

    @Autowired
    UserDocumentService userDocumentService;

    @Autowired
    MessageSource messageSource;

    @Autowired
    FileValidator fileValidator;

    @InitBinder("fileBucket")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(fileValidator);
    }

    /**
     Denna metod kommer att lista alla befintliga användare.
     */
    @RequestMapping(value = { "/", "/list" }, method = RequestMethod.GET)
    public String listUsers(ModelMap model) {

        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "userslist";
    }

    /**
     * Denna metod kommer att ge mediet för att lägga till en ny användare.
     */
    @RequestMapping(value = { "/newuser" }, method = RequestMethod.GET)
    public String newUser(ModelMap model) {
        User user = new User();
        model.addAttribute("user", user);
        model.addAttribute("edit", false);
        return "registration";
    }

    /**
     Denna metod kommer att kalla på formulär och posthantering.
     Spara användaren i databasen samt bekräfta användarinmatningen.
     */
    @RequestMapping(value = { "/newuser" }, method = RequestMethod.POST)
    public String saveUser(@Valid User user, BindingResult result,
                           ModelMap model) {

        if (result.hasErrors()) {
            return "registration";
        }

        if(!userService.isUserSSOUnique(user.getId(), user.getSsoId())){
            FieldError ssoError =new FieldError("user","ssoId",messageSource.getMessage("non.unique.ssoId", new String[]{user.getSsoId()}, Locale.getDefault()));
            result.addError(ssoError);
            return "registration";
        }

        userService.saveUser(user);

        model.addAttribute("user", user);
        model.addAttribute("success", "User " + user.getFirstName() + " "+ user.getLastName() + " registered successfully");
        //return "success";
        return "registrationsuccess";
    }


    /**
     Denna metod är till för att redigera och uppdatera en befintlig användare.
     */
    @RequestMapping(value = { "/edit-user-{ssoId}" }, method = RequestMethod.GET)
    public String editUser(@PathVariable String ssoId, ModelMap model) {
        User user = userService.findBySSO(ssoId);
        model.addAttribute("user", user);
        model.addAttribute("edit", true);
        return "registration";
    }

    /**
     *Denna metod kommer att kalla på formulär och posthantering.
     *Uppdatering av användare i databasen . Det bekräftar även användarinmatningen.
     */
    @RequestMapping(value = { "/edit-user-{ssoId}" }, method = RequestMethod.POST)
    public String updateUser(@Valid User user, BindingResult result,
                             ModelMap model, @PathVariable String ssoId) {

        if (result.hasErrors()) {
            return "registration";
        }

        userService.updateUser(user);

        model.addAttribute("success", "User " + user.getFirstName() + " "+ user.getLastName() + " updated successfully");
        return "registrationsuccess";
    }


    /**
     Denna metod kommer att ta bort en användare av dess SSOID värde.
     */
    @RequestMapping(value = { "/delete-user-{ssoId}" }, method = RequestMethod.GET)
    public String deleteUser(@PathVariable String ssoId) {
        userService.deleteUserBySSO(ssoId);
        return "redirect:/list";
    }


    /**
     * Denna metod lägger till document och sparar det dokumentet till den specifika användaren som dokumentet las till för.
     * Dokumentet sparas i UserDocument och returnerar managedocuments.
     */
    @RequestMapping(value = { "/add-document-{userId}" }, method = RequestMethod.GET)
    public String addDocuments(@PathVariable int userId, ModelMap model) {
        User user = userService.findById(userId);
        model.addAttribute("user", user);

        FileBucket fileModel = new FileBucket();
        model.addAttribute("fileBucket", fileModel);

        List<UserDocument> documents = userDocumentService.findAllByUserId(userId);
        model.addAttribute("documents", documents);

        return "managedocuments";
    }


    /**
     * Denna metod gör att man kan ladda ner ett document som man har lagt till.
     */
    @RequestMapping(value = { "/download-document-{userId}-{docId}" }, method = RequestMethod.GET)
    public String downloadDocument(@PathVariable int userId, @PathVariable int docId, HttpServletResponse response) throws IOException {
        UserDocument document = userDocumentService.findById(docId);
        response.setContentType(document.getType());
        response.setContentLength(document.getContent().length);
        response.setHeader("Content-Disposition","attachment; filename=\"" + document.getName() +"\"");

        FileCopyUtils.copy(document.getContent(), response.getOutputStream());

        return "redirect:/add-document-"+userId;
    }

    /**
     * Denna metod gör att man kan radera dokument som man har lagt till.
     */

    @RequestMapping(value = { "/delete-document-{userId}-{docId}" }, method = RequestMethod.GET)
    public String deleteDocument(@PathVariable int userId, @PathVariable int docId) {
        userDocumentService.deleteById(docId);
        return "redirect:/add-document-"+userId;
    }

    /**
     * Den kollar direkt att userdokument inte har några fel ex: om dokumentet inte är för stort.
     */
    @RequestMapping(value = { "/add-document-{userId}" }, method = RequestMethod.POST)
    public String uploadDocument(@Valid FileBucket fileBucket, BindingResult result, ModelMap model, @PathVariable int userId) throws IOException{

        if (result.hasErrors()) {
            System.out.println("validation errors");
            User user = userService.findById(userId);
            model.addAttribute("user", user);

            List<UserDocument> documents = userDocumentService.findAllByUserId(userId);
            model.addAttribute("documents", documents);

            return "managedocuments";
        } else {

            System.out.println("Fetching file");

            User user = userService.findById(userId);
            model.addAttribute("user", user);

            saveDocument(fileBucket, user);

            return "redirect:/add-document-"+userId;
        }
    }

    /**
     * Denna metod sparar dokumentet.
     */
    private void saveDocument(FileBucket fileBucket, User user) throws IOException{

        UserDocument document = new UserDocument();

        MultipartFile multipartFile = fileBucket.getFile();

        document.setName(multipartFile.getOriginalFilename());
        document.setDescription(fileBucket.getDescription());
        document.setType(multipartFile.getContentType());
        document.setContent(multipartFile.getBytes());
        document.setUser(user);
        userDocumentService.saveDocument(document);
    }

}

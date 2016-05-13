package se.ozgur.nackademin.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.ozgur.nackademin.dao.UserDao;
import se.ozgur.nackademin.model.User;


@Service("userService")
@Transactional
public class UserServiceImpl implements UserService{

    @Autowired
    private UserDao dao;

    /**
     * Returnera id
     */
    public User findById(int id) {
        return dao.findById(id);
    }

    /**
     * Hitta SSO och returnerar user.
     */
    public User findBySSO(String sso) {
        User user = dao.findBySSO(sso);
        return user;
    }

    public void saveUser(User user) {
        dao.save(user);
    }

    /**
       Eftersom metoden är igång med transaktion , utan att behöva kalla på Hibernate uppdatering explicit .
       Bara hämta entity från db och uppdatera den med riktiga värden inom transaktionen .
       Det kommer att uppdateras i db när transaktionen avslutas .
     */
    public void updateUser(User user) {
        User entity = dao.findById(user.getId());
        if(entity!=null){
            entity.setSsoId(user.getSsoId());
            entity.setFirstName(user.getFirstName());
            entity.setLastName(user.getLastName());
            entity.setEmail(user.getEmail());
            entity.setUserDocuments(user.getUserDocuments());
        }
    }

    /**
     * Den tar bort en användare som identiferas med hjälp av SSO
     */
    public void deleteUserBySSO(String sso) {
        dao.deleteBySSO(sso);
    }

    /**
     * Returnerar en lista med alla användare
     */
    public List<User> findAllUsers() {
        return dao.findAllUsers();
    }

    /**
     * Den kollar om SSO:n är unik
     */
    public boolean isUserSSOUnique(Integer id, String sso) {
        User user = findBySSO(sso);
        return ( user == null || ((id != null) && (user.getId() == id)));
    }

}

package se.ozgur.nackademin.dao;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import se.ozgur.nackademin.model.User;


@Repository("userDao")
public class UserDaoImpl extends AbstractDao<Integer, User> implements UserDao {

    /**
     * Denna metod hittar användaren via ID och returnerar användaren.
     */
    public User findById(int id) {
        User user = getByKey(id);
        return user;
    }

    /**
     * Denna metod hittar användaren via SSO alltså ID och sparar användaren i en Criteria och returnerar User.
     */
    public User findBySSO(String sso) {
        System.out.println("SSO : "+sso);
        Criteria crit = createEntityCriteria();
        crit.add(Restrictions.eq("ssoId", sso));
        User user = (User)crit.uniqueResult();
        return user;
    }

    /**
     *
     * Söker alla användare i Criterian och returnerar alla användare.
     */
    @SuppressWarnings("unchecked")
    public List<User> findAllUsers() {
        Criteria criteria = createEntityCriteria().addOrder(Order.asc("firstName"));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);//To avoid duplicates.
        List<User> users = (List<User>) criteria.list();

        return users;
    }

    /**
     * Spara användare
     */
    public void save(User user) {
        persist(user);
    }

    /**
     *
     * Denna metod är till för att ta bort användare
     */
    public void deleteBySSO(String sso) {
        Criteria crit = createEntityCriteria();
        crit.add(Restrictions.eq("ssoId", sso));
        User user = (User)crit.uniqueResult();
        delete(user);
    }

}

package se.ozgur.nackademin.dao;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import se.ozgur.nackademin.model.UserDocument;


@Repository("userDocumentDao")
public class UserDocumentDaoImpl extends AbstractDao<Integer, UserDocument> implements UserDocumentDao{

    /**
     * Returnerar en Criteria lista med User dokument
     */
    @SuppressWarnings("unchecked")
    public List<UserDocument> findAll() {
        Criteria crit = createEntityCriteria();
        return (List<UserDocument>)crit.list();
    }

    /**
     * Spara dokument
     */
    public void save(UserDocument document) {
        persist(document);
    }

    /**
     * Hittar dokument som tillhör en specifik User.
     */
    public UserDocument findById(int id) {
        return getByKey(id);
    }

    /**
     * Returnerar en lista av dokument
     */
    @SuppressWarnings("unchecked")
    public List<UserDocument> findAllByUserId(int userId){
        Criteria crit = createEntityCriteria();
        Criteria userCriteria = crit.createCriteria("user");
        userCriteria.add(Restrictions.eq("id", userId));
        return (List<UserDocument>)crit.list();
    }


    /**
     * Radera dokument
     */
    public void deleteById(int id) {
        UserDocument document =  getByKey(id);
        delete(document);
    }

}

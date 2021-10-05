package facades;

import dtos.AddressDTO;
import dtos.HobbyDTO;
import dtos.PersonDTO;
import dtos.PhoneDTO;
import entities.*;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import utils.EMF_Creator;

public class PersonFacade {

    private static PersonFacade instance;
    private static EntityManagerFactory emf;


    private PersonFacade() {

    }

    public static PersonFacade getFacadeExample(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PersonFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void main(String[] args) {
        emf = EMF_Creator.createEntityManagerFactory();
        PersonFacade fe = getFacadeExample(emf);
        fe.getAllPersons().forEach(dto -> System.out.println(dto));
    }




    public PersonDTO addPerson(PersonDTO personDTO) {
        EntityManager em = emf.createEntityManager();
        Person person = new Person(personDTO.getFirstName(),personDTO.getLastName(),personDTO.getEmail());

        //adding Phone/phones
        for (PhoneDTO phoneDTO: personDTO.getPhones()) {
            Phone phone = new Phone(phoneDTO.getPhoneNumber(),phoneDTO.getDescription());
            person.addPhone(phone);
        }

        //Adding hobby/hobbies
        for (HobbyDTO hobbyDTO: personDTO.getHobbies()) {
            Hobby hobby = new Hobby(hobbyDTO.getName(), hobbyDTO.getDescription());
            person.addHobby(hobby);
        }


        System.out.println(personDTO);
        //Adding Adress
        Address address = new Address(personDTO.getAddress().getStreet(),personDTO.getAddress().getAdditionalInfo());

        //adding CityInfo
        CityInfo cityInfo = new CityInfo(personDTO.getAddress().getCityInfoDTO().getZipCode(),personDTO.getAddress().getCityInfoDTO().getCity());

        address.addPerson(person);
        //address.setCityInfo(cityInfo);
        cityInfo.addAddress(address);



        try {
            em.getTransaction().begin();
            em.persist(cityInfo);
          //  address.setCityInfo(cityInfo);
           // em.persist(address);
           // em.merge(address);
           // em.persist(person);
           // person.setAddress(address);
           // em.merge(person);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return new PersonDTO(person);
    }

    public PersonDTO getPerson(int id) {
        EntityManager em = emf.createEntityManager();
        return new PersonDTO(em.find(Person.class, id));
    }

    public List<PersonDTO> getAllPersons() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Person> query = em.createQuery("SELECT p FROM Person p", Person.class);
        List<Person> rms = query.getResultList();
        return PersonDTO.getDtos(rms);
    }

    public long getPersonCount(){
        EntityManager em = emf.createEntityManager();
        try {
            long personCount = (long)em.createQuery("SELECT COUNT(p) FROM Person p").getSingleResult();
            return personCount;
        } finally {
            em.close();
        }
    }

    //Get all persons with a given hobby
    //fodbold --> List person
    public List<PersonDTO> getAllPersonsByHobby(String hobby){
        EntityManager em = emf.createEntityManager();
        List<Person> persons = em
                .createQuery("SELECT p FROM Person p JOIN p.hobbies h WHERE h.name = :hobby", Person.class)
                .setParameter("hobby", hobby)
                .getResultList();
        return PersonDTO.getDtos(persons);
    }
}

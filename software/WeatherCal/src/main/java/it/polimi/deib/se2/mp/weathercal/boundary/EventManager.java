/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.deib.se2.mp.weathercal.boundary;

import it.polimi.deib.se2.mp.weathercal.entity.Event;
import it.polimi.deib.se2.mp.weathercal.entity.Participation;
import it.polimi.deib.se2.mp.weathercal.entity.TimeZoneResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author paolo
 */
@Stateless
public class EventManager extends AbstractFacade<Event>{
    
    private final static String GOOGLE_TIMEZONE_API = "https://maps.googleapis.com/maps/api/timezone/json?"
            + "location={latitude},{longitude}&timestamp={ts}";
    private Map<String, Object> params;

    @PersistenceContext
    EntityManager em;

    @EJB
    UserManager um;

    @Inject
    private Logger logger;
    
    private Client client;
    
    public EventManager() {
        super(Event.class);
        client = ClientBuilder.newClient();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    private TimeZoneResponse fetchTimezone(String lat, String lng, String ts) {
        params = new HashMap<String, Object>(){{
            put("latitude", lat);
            put("longitude", lng);
            put("ts", ts);
        }};
        return client.target(GOOGLE_TIMEZONE_API)
                .resolveTemplates(params)
                .request(MediaType.APPLICATION_JSON)
                .get(TimeZoneResponse.class);
    }
    
    /**
     *
     * Calculates the offset from the UTC for the locality set for the Event e at the inputed LocalDateTime ts.
     * 
     * @param e the interested Event
     * @param ts the inputed LocalDateTime
     * @return the minutes of the offset
     */
    public int getTimezoneOffset(Event e, LocalDateTime ts){
        TimeZoneResponse tz = fetchTimezone(
                e.getPlaceLatitude().toPlainString(),
                e.getPlaceLongitude().toPlainString(),
                Long.toString(ts.toEpochSecond(ZoneOffset.UTC))
        );
        return (tz.getDstOffset() + tz.getRawOffset()) / 60;
    }
    
    /**
     *
     * @param resultOffset the offset expressed in minutes
     * @return
     */
    public ZoneOffset getTimezone(int resultOffset) {
        return ZoneOffset.of(
                String.format("%.1s%02d:%02d", resultOffset < 0? "-": "+",
                        Math.abs(resultOffset / 60), resultOffset % 60)
        );
    }

    public void save(Event event) {
        
        em.persist(event);
//        em.flush();
//        Query q = em.createNamedQuery("Groups.findByName");
//        Groups g = (Groups) q
//                    .setParameter("name", Groups.USERS)
//                    .getSingleResult();
//        if(!user.getGroupsCollection().contains(g)){
//            user.getGroupsCollection().add(g);
//            em.merge(user);
//        }
    }
    public void changeAvailability(String av,Participation changepart){
  
        
        changepart.setAvailability(av);
        em.merge(changepart);
        em.flush();
        
      //  em.merge(changepart);

        System.out.println("sssss" + changepart.getAvailability());
    }
    public List<Event> tGetAll(){
        Query q = em.createNamedQuery("Event.findAll");
        return q.getResultList();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
    
}

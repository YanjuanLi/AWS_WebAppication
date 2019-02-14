package com.cloud.assignment.assignment.Note;
//

import com.cloud.assignment.assignment.webSource.BCrypt;
import com.cloud.assignment.assignment.webSource.User;
import com.cloud.assignment.assignment.webSource.UserRepository;

import com.cloud.assignment.assignment.webSource.Authorization;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@RestController
public class NoteController {
    @Autowired // This means to get the bean called noteRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    Authorization authorization = new Authorization();





    @GetMapping(path = "/note")
    public Object getAllNote(@RequestHeader String Authorization, Note newNote, HttpServletResponse response) {

        //Authorization authorization = new Authorization();

        User user = authorization.authorizeUser(Authorization);

        //User user = authorizeUser(Authorization);


            if(user!=null) {


                List<Note> noteList = noteRepository.findAllByUser(user);
                List resultList = new ArrayList();


                if(noteList.isEmpty()){
                    response.setStatus(404);
                    return "{\"Not Found\"}";
                }
                else {

                    response.setStatus(200);
                    return noteList;
                }
            }

            else {

                response.setStatus(401);
                return "{\"Unauthorized\"}";
            }

    }


    @PostMapping("/note")
    public String CreateNote(@RequestBody Note newNote, HttpServletResponse response, User newUser, @RequestHeader  String Authorization) {




        User user = authorization.authorizeUser(Authorization);

       // User user = authorizeUser(Authorization);

         if(user!=null){



                if(newNote.getTitle().equals("") || newNote.getContent().equals("") || newNote.getTitle().length()>=20)
                {
                    response.setStatus(400);
                    return "{\"Bad Request\"}";
                }

                else{


                    //newNote.setNoteId(UUID.randomUUID());

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

                    newNote.setNoteId(UUID.randomUUID().toString());

                    newNote.setCreated_on(sdf.format(new Date()));

                    // get the create date for later use
                    String createdate = newNote.getCreated_on();

                    newNote.setLast_updated_on(sdf.format(new Date()));

                    newNote.setUser(user);

                    response.setStatus(200);
                    noteRepository.save(newNote);
                    return "{\"saved\"}";


                }

            }
            else {

                response.setStatus(401);
                return "{\"Unauthorized\"}";
            }
    }

    @RequestMapping(value = "/note/{id}", method = RequestMethod.GET)

    public Object getSingleNote(@RequestHeader String Authorization,@PathVariable("id") String id, HttpServletResponse response) {
        User user = authorization.authorizeUser(Authorization);
        String realId = id.substring(1,id.length()-1);
        if (user == null){
            response.setStatus(401);
            return "{\"Unauthorized\"}";
        }else {
            if (realId.equals("")){
                response.setStatus(400);
                return"{\"Bad Request\"}";
            }else{
                List<Note> list = noteRepository.findAllByUser(user);
                for(int i = 0; i<list.size();i++){
                    if(list.get(i).getNoteId().equals(realId)) {
                        response.setStatus(200);
                        return list.get(i);
                    }
                }
            }
        }
        response.setStatus(404);
        return "{\"Not Found\"}";
    }



    @RequestMapping(value="/note/{id}",method=RequestMethod.PUT)

        public String update(@PathVariable("id") String id,@RequestBody Note note, HttpServletResponse response,@RequestHeader String Authorization){
        //User user = authorizeUser(Authorization);

        User user = authorization.authorizeUser(Authorization);

        if(user==null){
            response.setStatus(404);
            return "{\"Not Found\"}";
        }else{
        List<Note> noteList = noteRepository.findAllByUser(user);
        String realId = id.substring(1,id.length()-1);

        if(realId.equals("")){
            response.setStatus(400);
            return "{\"Bad Request\"}";
        }else{
        Note note2 = new Note();
        for(int i=0;i<noteList.size();i++) {
            if (realId.equals(noteList.get(i).getNoteId())) {
                note2 = noteList.get(i);

                if(note.getContent().equals("")||note.getTitle().equals(""))
                {
                    response.setStatus(400);
                    return "{\"Bad Request\"}";

                }
                else if (note.getContent().equals(note2.getContent()) && note.getTitle().equals(note2.getTitle())) {

                    response.setStatus(400);
                    return "{\"Bad Request\"}";
                } else {
                    SimpleDateFormat updateTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    note2.setLast_updated_on(updateTime.format(new Date()));
                    note2.setTitle(note.getTitle());
                    note2.setContent(note.getContent());
                    noteRepository.save(note2);

                    response.setStatus(204);
                    return "{\"No Content\"}";
                }
            }
            }
        }

        }
        response.setStatus(404);
        return "{\"Not Found\"}";
    }

    @RequestMapping (value="/note/{id}",method=RequestMethod.DELETE)
    public @ResponseBody
    String deleteNote(@PathVariable("id") String id, HttpServletResponse response,
                      @RequestHeader String Authorization) {

        User user = authorization.authorizeUser(Authorization);

        String realId = id.substring(1,id.length()-1);

        //User user = authorizeUser(Authorization);

        if (user == null) {
            response.setStatus(401);
            return "{\"Unauthorized\"}";
        } else {
            List<Note> list = noteRepository.findAllByUser(user);
            if (list.size() < 1) {
                response.setStatus(404);
                return "{\"Not Found\"}";
            } else {
                for (Note note : list) {
                    if (note.getNoteId().equals(realId)) {
                        noteRepository.delete(note);
                        //response.setStatus(200);
                        response.setStatus(204);
                        return "{\"No Content\"}";
                    }
                    else if(realId.equals(""))
                    {
                        response.setStatus(400);
                        return "{\"Bad Request\"}";
                    }
                    else {
                        response.setStatus(404);
                        return "{\"Not Found\"}";
                    }
                }
            }
            return null;
        }
    }

}


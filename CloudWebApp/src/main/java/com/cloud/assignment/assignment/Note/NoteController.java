package com.cloud.assignment.assignment.Note;
//

import com.cloud.assignment.assignment.AmazonS3_dev.AmazonS3Client;
import com.cloud.assignment.assignment.Attachment.Attachment;
import com.cloud.assignment.assignment.Attachment.AttachmentRepository;
import com.cloud.assignment.assignment.webSource.User;
import com.cloud.assignment.assignment.webSource.UserRepository;
import com.cloud.assignment.assignment.Metrics.MetricsClientBean;

import com.cloud.assignment.assignment.webSource.Authorization;

import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.*;
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
    private AttachmentRepository attachmentRepository;

    @Autowired
    Authorization authorization = new Authorization();

    @Autowired
    private AmazonS3Client amazonClient;

    @Autowired
    private StatsDClient statsDClient;





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
                    return "{\n \"description\":\"Note Not Found\" \n}";
                }
                else {

                    response.setStatus(200);
                    return noteList;
                }
            }

            else {

                response.setStatus(401);
                return "{\n \"description\":\"Unauthorized\" \n}";
            }

    }


    @PostMapping("/note")
    public Object createNote(@RequestBody Note newNote, HttpServletResponse response, User newUser, @RequestHeader  String Authorization) {




        User user = authorization.authorizeUser(Authorization);

       // User user = authorizeUser(Authorization);

         if(user!=null){



                if(newNote.getTitle().equals("") || newNote.getContent().equals("") || newNote.getTitle().length()>=20)
                {
                    response.setStatus(400);
                    return "{\n \"description\":\"Bad Request\" \n}";
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

                    response.setStatus(201);
                    noteRepository.save(newNote);
                    //return "{\"Created\"}";
                    return (newNote);


                }

            }
            else {

                response.setStatus(401);
                return "{\n \"description\":\"Unauthorized\" \n}";
            }
    }

    @RequestMapping(value = "/note/{id}", method = RequestMethod.GET)

    public Object getSingleNote(@RequestHeader String Authorization,@PathVariable("id") String id, HttpServletResponse response) {
        User user = authorization.authorizeUser(Authorization);

        if (user == null){
            response.setStatus(401);
            return "{\n \"description\":\"Unauthorized\" \n}";
        }else {
            if (id.equals("")){
                response.setStatus(400);
                return"{\n \"description\":\"Bad Request\" \n}";
            }else{
                List<Note> list = noteRepository.findAllByUser(user);
                for(int i = 0; i<list.size();i++){
                    if(list.get(i).getNoteId().equals(id)) {
                        response.setStatus(200);
                        return list.get(i);
                    }
                }
                response.setStatus(404);
                return "{\n \"description\":\"Note Not Found\" \n}";
            }
        }

    }



    @RequestMapping(value="/note/{id}",method=RequestMethod.PUT)

        public String update(@PathVariable("id") String id,@RequestBody Note note, HttpServletResponse response,@RequestHeader String Authorization){
        //User user = authorizeUser(Authorization);

        User user = authorization.authorizeUser(Authorization);

        if(user==null){
            response.setStatus(401);
            return "{\n \"description\":\"Unauthorized\" \n}";
        }else{
        List<Note> noteList = noteRepository.findAllByUser(user);


        if(id.equals("")){
            response.setStatus(400);
            return "{\n \"description\":\"Bad Request\" \n}";
        }else{
        Note note2 = new Note();
        for(int i=0;i<noteList.size();i++) {
            if (id.equals(noteList.get(i).getNoteId())) {
                note2 = noteList.get(i);

                if(note.getContent().equals("")||note.getTitle().equals(""))
                {
                    response.setStatus(400);
                    return "{\n \"description\":\"Bad Request\" \n}";

                }
                else if (note.getContent().equals(note2.getContent()) && note.getTitle().equals(note2.getTitle())) {

                    response.setStatus(400);
                    return "{\n \"description\":\"Bad Request\" \n}";
                }




                else {
                    SimpleDateFormat updateTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    note2.setLast_updated_on(updateTime.format(new Date()));
                    note2.setTitle(note.getTitle());
                    note2.setContent(note.getContent());
                    noteRepository.save(note2);

                    response.setStatus(204);
                    return null;
                }




            }


        }
            response.setStatus(404);
            return "{\n \"description\":\"Not Found\" \n}";


        }



        }

    }



    @Transactional

    @RequestMapping (value="/note/{id}",method=RequestMethod.DELETE)
    public @ResponseBody
    String deleteNote(@PathVariable("id") String id, HttpServletResponse response,
                      @RequestHeader String Authorization) {

        User user = authorization.authorizeUser(Authorization);



        //User user = authorizeUser(Authorization);

        if (user == null) {
            response.setStatus(401);
            return "{\n \"description\":\"Unauthorized\" \n}";
        } else {

            List<Note> list = noteRepository.findAllByUser(user);



            if (list.size() < 1) {
                response.setStatus(404);
                return "{\n \"description\":\"Not Found\" \n}";
            } else {
                //for (Note note : list) {
                //Note note2 = new Note();
               Note note2 =noteRepository.findByNoteId(id);

                for(int i = 0; i <list.size(); i++)
                    {
                        //Note note = list.get(i);

                    //if (note.getNoteId().equals(realId)) {


                        if(id.equals(list.get(i).getNoteId())){



                            note2 = list.get(i);

                            //List<Attachment> attachlist = optionalNote.get().getAttachments();

                            List<Attachment> attachementList = attachmentRepository.findAllByNote(note2);

                            for (Attachment attach : attachementList){

                                if (amazonClient.getEndpointUrl().equals("")){
                                    amazonClient.deleteFileFromLocal(attach);
                                }
                                else{
                                    amazonClient.deleteFileFromS3Bucket(attach.getUrl());
                                }
                                attachmentRepository.deleteById(attach.getId());
                            }

                            note2 = list.get(i);

                            noteRepository.delete(note2);
                        //response.setStatus(200);
                        response.setStatus(204);
                        return null;
                    //    return "{\"No Content\"}";
                    }
                    else if(id.equals(""))
                    {
                        response.setStatus(400);
                        return "{\n \"description\":\"Bad Request\" \n}";
                    }


//                    else {
//                        response.setStatus(404);
//                        return "{\"Not Found\"}";
//                    }
                }
                response.setStatus(404);
                return "{\n \"description\":\"Not Found\" \n}";
            }

            //return null;
        }
    }

    @GetMapping(path = "/note/{idNotes}/attachments")
    public Object getAllAttachment(@PathVariable("idNotes") String idNotes,
             @RequestHeader String Authorization, Note newNote, HttpServletResponse response) {



        User user = authorization.authorizeUser(Authorization);


        if(user!=null) {


            List<Note> noteList = noteRepository.findAllByUser(user);


            if (idNotes.equals("")) {
                response.setStatus(400);
                return "{\n \"description\":\"Bad Request\" \n}";
            }else {
                if (noteList.isEmpty()) {
                    response.setStatus(404);
                    return "{\n \"description\":\"Not Found\" \n}";
                } else {

                    for (Note n : noteList) {
                        if (n.getNoteId().equals(idNotes)) {

                            response.setStatus(200);
                            //return n.getAttachments();
                            return attachmentRepository.findAllByNote(n);
                        }
                    }
                    response.setStatus(404);
                    return "{\n \"description\":\"Not Found\" \n}";
                }

            }
        }

        else {

            response.setStatus(401);
            return "{\n \"description\":\"Unauthorized\" \n}";
        }

    }



    @RequestMapping(value = "/note/{idNotes}/attachments", method = RequestMethod.POST)
    public Object createAttachment(@PathVariable("idNotes") String idNotes, @RequestParam(value = "file",required = false) MultipartFile file, HttpServletResponse response, User newUser,Attachment attachment, @RequestHeader  String Authorization) {




        User user = authorization.authorizeUser(Authorization);

        if(user!=null) {

            List<Note> noteList = noteRepository.findAllByUser(user);


            if (idNotes.equals("")) {
                response.setStatus(400);
                return "{\n \"description\":\"Bad Request\" \n}";
            }else{
            if (noteList.isEmpty()) {
                response.setStatus(404);
                return "{\n \"description\":\"Not Found\" \n}";
            } else {

                Note note = noteRepository.findByNoteId(idNotes);
                if(note!=null){

                //for (Note note : noteList)
                //{


//                    String filename = file.getOriginalFilename();
//                    String filegetUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(filename).toUriString();
                    String filegetUrl  = new String();



                    if (note.getNoteId().equals(idNotes)) {


                        if (amazonClient.getEndpointUrl().equals("")) {
                            String filename = new Date().getTime()+"-"+file.getOriginalFilename();
                            String path = new String();
                            try {
                                path = ResourceUtils.getURL("classpath:").getPath();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            try {
                                BufferedOutputStream out = new BufferedOutputStream(
                                        new FileOutputStream(new File(path+filename)));
                                System.out.println(file.getName());
                                out.write(file.getBytes());
                                out.flush();
                                out.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            filegetUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                                    .path(filename)
                                    .toUriString();
                        }
                        else{
                            filegetUrl = amazonClient.uploadFile(file);
                        }

                        //Attachment attachment3 = attachmentRepository.findByUrl(filegetUrl);

                        //attachment = note.addAttachment();
                        Attachment attachment1 = attachmentRepository.findByUrl(filegetUrl);

                        if(attachment1==null) {

                            attachment.setId(UUID.randomUUID().toString());
                            attachment.setNote(note);
                            attachment.setUrl(filegetUrl);
                            attachmentRepository.save(attachment);

                            response.setStatus(200);
                            return attachment;
                        }
                        else
                        {
                            response.setStatus(400);
                            return "{\n \"description\":\"File Already Exist\" \n}";
                        }

                    }
                    else if (idNotes.equals(""))
                    {
                        response.setStatus(400);
                        return "{\n \"description\":\"Bad Request\" \n}";
                    }

                }
                else {

                    response.setStatus(404);
                    return "{\n \"description\":\"Not Found\" \n}";
                }

            }


        }

        }
        else {

            response.setStatus(401);
            return "{\n \"description\":\"Unauthorized\" \n}";
        }
        return null;
    }


    @RequestMapping(value="/note/{idNotes}/attachments/{idAttachments}",method=RequestMethod.PUT)
    public String updateAttachment(@RequestParam(value = "file",required = false) MultipartFile file,Attachment attachment, @PathVariable("idNotes") String idNotes,@PathVariable("idAttachments") String idAttachments, HttpServletResponse response,@RequestHeader String Authorization) {

        User user = authorization.authorizeUser(Authorization);

        if (user == null) {
            response.setStatus(401);
            return "{\n \"description\":\"Unauthorized\" \n}";
        } else {
            List<Note> noteList = noteRepository.findAllByUser(user);

            if (idNotes.equals("") || idAttachments.equals("")) {
                response.setStatus(400);
                return "{\n \"description\":\"Bad Request\" \n}";
            } else if (noteList.size() < 1) {
                response.setStatus(404);
                return "{\n \"description\":\"Note Not Found\" \n}";
            }
                else {

                for (Note note2 : noteList) {
                        if (note2.getNoteId().equals(idNotes)) {

                            //Attachment attachment = note2.getSingleAttachment(idAttachments);

                            List attachementList = attachmentRepository.findAllByNote(note2);

                            attachment = attachmentRepository.findById(idAttachments);
                            if (attachementList.size() == 0) {
                                response.setStatus(404);
                                return "{\n \"description\":\"Attachment Not Found\" \n}";
                            } else {

//                                String filename = file.getOriginalFilename();
//                                String filegetUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(filename).toUriString();

                                String filegetUrl  = new String();
                                String path = null;


                                if (amazonClient.getEndpointUrl().equals("")) {
                                    amazonClient.deleteFileFromLocal(attachment);
                                    String filename = new Date().getTime()+"-"+file.getOriginalFilename();
                                    try {

                                        BufferedOutputStream out = new BufferedOutputStream(

                                                new FileOutputStream(new File(path+filename)));
                                        System.out.println(file.getName());
                                        out.write(file.getBytes());
                                        out.flush();
                                        out.close();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    filegetUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                                            .path(filename)
                                            .toUriString();
                                }
                                else{
                                    String url = attachment.getUrl();
                                    amazonClient.deleteFileFromS3Bucket(url);
                                    filegetUrl = amazonClient.uploadFile(file);
                                }


                                attachment = attachmentRepository.findById(idAttachments);
                                Attachment attachment1 = attachmentRepository.findByUrl(filegetUrl);

                                //if((attachment1!=null && attachment1.getId().equals(idAttachments))|| attachment1==null)

                                if(attachment.getId().equals(idAttachments))
                                {

                                    attachment.setUrl(filegetUrl);
                                    attachmentRepository.save(attachment);

                                    response.setStatus(204);
                                    return null;

                                }

//                                if() {
//                                    attachment.setUrl(filegetUrl);
//
//                                    attachmentRepository.save(attachment);
//
//                                    response.setStatus(204);
//                                    return null;
//                                }


                                else {

                                    response.setStatus(400);
                                    return "{\n \"description\":\"File Already Exist\" \n}";

                                }
                            }

                        }
                    }
                response.setStatus(404);
                return "{\n \"description\":\"Note Not Found\" \n}";


                }


        }
    }


    @Transactional

    @RequestMapping (value="/note/{idNotes}/attachments/{idAttachments}",method=RequestMethod.DELETE)
    public String deleteAttachment(@PathVariable("idNotes") String idNotes, @PathVariable("idAttachments") String idAttachments,HttpServletResponse response,
                      @RequestHeader String Authorization) {

        User user = authorization.authorizeUser(Authorization);

        if (user == null) {
            response.setStatus(401);
            return "{\n \"description\":\"Unauthorized\" \n}";
        } else {

            List<Note> list = noteRepository.findAllByUser(user);


            if(idNotes.equals("")||idAttachments.equals("")){
                response.setStatus(400);
                return "{\n \"description\":\"Bad Request\" \n}";
            }else{
                if (list.size() < 1) {
                    response.setStatus(404);
                    return "{\n \"description\":\"Note Not Found\" \n}";
                } else {

                    for(Note note2:list){
                        if(note2.getNoteId().equals(idNotes)){

                            //Attachment attachment = note2.getSingleAttachment(idAttachments);
                            List <Attachment> attcahmentList = attachmentRepository.findAllByNote(note2);
                            if(attcahmentList.size() == 0){
                                response.setStatus(404);
                                return "{\n \"description\":\"Attachment Not Found\" \n}";
                            }else{
                                //note2.deleteAttachment(idAttachments);

                                for(int i= 0; i < attcahmentList.size(); i++){

                                    if(attcahmentList.get(i).getId().equals(idAttachments))
                                    {




                                        if (amazonClient.getEndpointUrl().equals("")){
                                            amazonClient.deleteFileFromLocal(attcahmentList.get(i));
                                        }
                                        else{
                                            amazonClient.deleteFileFromS3Bucket(attcahmentList.get(i).getUrl());
                                        }


                                        attachmentRepository.delete(attcahmentList.get(i));
                                        response.setStatus(204);
                                        return null;

                                    }


                                }

                            }

                        }

                    }
                    response.setStatus(404);
                    return "{\n \"description\":\" Note Not Found\" \n}";
                }



            }

        }
    }
}


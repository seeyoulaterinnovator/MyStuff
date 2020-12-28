package ru.alamics.sso.keycloak.user.resource.ls;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.user.PersonalAccountService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Slf4j
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class PersonalAccountResource {

    private final PersonalAccountService service;

    public PersonalAccountResource(PersonalAccountService service) {
        this.service = service;
    }

    // запросить все лс для должности
    // GET all
    @GET
    @Path("/{postId}")
    @NoCache
    public Response get(@PathParam("postId") String postId) {
        try {
            return JsonResponse.success()
                    .addResult("data", service.getAccountModel(postId))
                    .build();

        } catch (/*NotFoundException*/ Exception e) {
            log.error("", e);
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }

    // поставить все лс для должности
    // POST / PUT
    @POST
    @Path("/{postId}")
    public Response set(@PathParam("postId") String postId, List<String> paList) {
        try {
            service.setAccountList(postId, paList);
            return JsonResponse.success()
                    .addResult("result", 1)
                    .build();

        } catch (/*NotFoundException*/ Exception e) {
            log.error("", e);
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }

    // добавить лс для должности
    // PATCH
    // TODO убирать дубли
    @PATCH
    @Path("/{postId}/add")
    public Response add(@PathParam("postId") String postId, List<String> paList) {
        try {
            service.addAccountList(postId, paList);
            return JsonResponse.success()
                    .addResult("result", 1)
                    .build();

        } catch (/*NotFoundException*/ Exception e) {
            log.error("", e);
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }

    // убрать часть лс из должности
    // PATCH ?
    @PATCH
    @Path("/{postId}/sub")
    public Response sub(@PathParam("postId") String postId, List<String> paUuidList) {
        try {
            service.subAccountUuidList(postId, paUuidList);
            return JsonResponse.success()
                    .addResult("result", 1)
                    .build();

        } catch (/*NotFoundException*/ Exception e) {
            log.error("", e);
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }

    // очистить все лс из должности
    // DELETE
    @DELETE
    @Path("/{postId}")
    public Response delete(@PathParam("postId") String postId) {
        try {
            service.deleteAccountList(postId);
            return JsonResponse.success()
                    .addResult("result", 1)
                    .build();

        } catch (/*NotFoundException*/ Exception e) {
            log.error("", e);
            return JsonResponse.fail()
                    .message(e.getMessage())
                    .build();
        }
    }
}

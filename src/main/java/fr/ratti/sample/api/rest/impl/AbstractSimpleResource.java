package fr.ratti.sample.api.rest.impl;

import fr.ratti.sample.api.annotation.Seachable;
import fr.ratti.sample.api.exception.MandatoryFieldsNotSetException;
import fr.ratti.sample.api.exception.ResourceAllreadyExistsException;
import fr.ratti.sample.api.exception.ResourceNotFoundException;
import fr.ratti.sample.api.model.Container;
import fr.ratti.sample.api.services.IDService;
import fr.ratti.sample.api.services.ResourceManagerService;
import fr.ratti.sample.api.tools.DataModelControler;
import org.apache.cxf.jaxrs.ext.PATCH;
import org.slf4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.*;

/**
 * Created by bratti on 17/08/2016.
 */
public abstract class AbstractSimpleResource<T> {

    protected IDService idService;
    protected String baseURI;
    protected int defaultRange = 100;

    protected abstract Logger getLogger();

    private ResourceManagerService<T> resourceManagerService;

    protected DataModelControler<T> dataModelControler = new DataModelControler<T>();

    public String getBaseURI() {
        return baseURI;
    }


    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public ResourceManagerService<T> getResourceManagerService() {
        return resourceManagerService;
    }

    public void setResourceManagerService(ResourceManagerService<T> resourceManagerService) {
        this.resourceManagerService = resourceManagerService;
    }

    public int getDefaultRange() {
        return defaultRange;
    }

    public void setDefaultRange(int defaultRange) {
        this.defaultRange = defaultRange;
    }

    protected void checkIdFormat(String id) {

        if (id == null) {
            throwWebApplicationException(BAD_REQUEST, "null_id", "the resource id is null");
        }

        try {
            idService.validate(id);
        } catch (Exception exception) {
            getLogger().error("format id is not valid for " + id);

            throwWebApplicationException(BAD_REQUEST, "id_format_invalid", "the resource id has invalid format");
        }
    }

    private void throwWebApplicationException(Response.Status status, String code, String description) {

        Map<String, String> errorMap = new HashMap<>(2);

        errorMap.put("error_code", code);
        errorMap.put("error_description", description);

        Response response = Response
                .status(status)
                .entity(errorMap)
                .build();

        throw new WebApplicationException(response);

    }


    public T getResource(String resourceId, UriInfo uriInfo) {

        checkIdFormat(resourceId);

        try {

            T resource = resourceManagerService.get(resourceId);
            setPartialResponse(resource, uriInfo);

            return resource;


        } catch (ResourceNotFoundException rnfe) {
            getLogger().error("resource " + resourceId + " not found", rnfe);
            throwWebApplicationException(NOT_FOUND, "resource_not_found", "the resource with id " + resourceId + " was not found");
        } catch (Exception e) {
            getLogger().error("exception has thrown when get resource " + resourceId);
            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
        }

        return null;

    }

    private void setPartialResponse(final T resource, UriInfo uriInfo) {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        if (queryParameters.containsKey("fields")) {

            deleteNotWantedFields(resource, queryParameters.getFirst("fields"));
        }
    }

    private void deleteNotWantedFields(Object resource, String wantedfields) {

        List<String> fields = new ArrayList<String>();
        Map<String, String> complexFields = new HashMap<String, String>();

        extractFields(wantedfields, fields, complexFields);

        Class clazz = resource.getClass();

        asList(clazz.getDeclaredFields()).stream()
                .filter(field -> !fields.contains(field.getName()))
                .forEach(field -> {

                    try {
                        field.setAccessible(true);
                        field.set(resource, null);
                    } catch (IllegalAccessException e) {
                        getLogger().warn("impossible to set null none selected attribute " + field.getName());
                    }

                });

        asList(clazz.getDeclaredFields()).stream()
                .filter(field -> complexFields.containsKey(field.getName()))
                .forEach(f -> {
                            try {
                                f.setAccessible(true);
                                String subFields = complexFields.get(f.getName());
                                deleteNotWantedFields(f.get(resource), subFields);
                            } catch (IllegalAccessException e) {
                                getLogger().warn("impossible to set null none selected attribute " + f.getName());
                            }

                        }
                );

    }

    private void extractFields(String strFields, List<String> fields, Map<String, String> complexeFields) {

        Pattern fieldPattern = Pattern.compile("[^(,]+\\([^()]+?\\)|[^(),]+");

        Matcher fieldsMatcher = fieldPattern.matcher(strFields);

        Pattern simpleField = Pattern.compile("[^(),]+");


        while (fieldsMatcher.find()) {

            String field = fieldsMatcher.group();

            if (simpleField.matcher(field).matches()) {
                fields.add(field);
            } else {
                String fieldName = field.substring(0, field.indexOf("("));
                String parameters = field.substring(field.indexOf("(") + 1, field.indexOf(")"));

                fields.add(fieldName);
                complexeFields.put(fieldName, parameters);

            }

        }
    }

    public Response updateResource(String id, T resource) {


        checkIdFormat(id);

        checkNotNullResource(resource);

        checkMandatoryFields(resource);

        // check if resource exists
        try {
            T existingResource = resourceManagerService.get(id);
            resourceManagerService.update(id, resource);

            Response response = Response.status(OK).build();
            return response;

        } catch (ResourceNotFoundException e) {

            try {
                resourceManagerService.create(id, resource);
                Response response = Response
                        .status(CREATED)
                        .header("Location", baseURI + getRelativePath(resource) + "/" + id)
                        .build();

                return response;

            } catch (Exception exception) {
                return handleException(exception);
            }

        } catch (Exception exception) {
            return handleException(exception);
        }

    }

    protected Response handleException(Exception e) {
        getLogger().error("create resource has thrown an unexpected exception", e);
        throw new WebApplicationException(INTERNAL_SERVER_ERROR);
    }

    private void checkMandatoryFields(T resource) {
        try {
            dataModelControler.checkMandatoryFields(resource);
        } catch (MandatoryFieldsNotSetException exception) {
            getLogger().error("Missing mandatory fields : ", exception);
            throwWebApplicationException(BAD_REQUEST, "mandatory_attributes_missing", "mandatory attribute(s) " + exception.getStringFieldNames() + " is/are missing on the resource");
        }
    }

    public Response patchResource(String resourceId, T resource) {


        checkIdFormat(resourceId);

        try {
            T existingResource = resourceManagerService.get(resourceId);

            // fill not null resource field to existingResource
            Class clazz = resource.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                try {
                    if (field.get(resource) != null) {
                        field.set(existingResource, field.get(resource));
                    }
                } catch (IllegalAccessException e) {
                    getLogger().warn("impossible to copy field " + field + " to resource " + resourceId, e);
                }
            }

            resourceManagerService.update(resourceId, existingResource);


        } catch (ResourceNotFoundException e) {
            throwWebApplicationException(NOT_FOUND, "resource_not_found", "the resource with id " + resourceId + " was not found");
        }

        return Response.status(OK).build();
    }


    protected String getRelativePath(T resource) {

        Class[] interfaces = this.getClass().getInterfaces();

        for (Class clazz : interfaces) {
            Path path = (Path) clazz.getAnnotation(Path.class);

            if (path != null) {
                String pathValue = path.value();
                if (pathValue != null && pathValue.startsWith("/")) {

                    return pathValue;
                } else {
                    return "/" + pathValue;
                }
            }
        }
        return "";
    }

    public Response createResource(T resource) {

        checkNotNullResource(resource);

        checkMandatoryFields(resource);

        String identifiant = null;
        Response.Status responseStatus = CREATED;


        try {
            identifiant = resourceManagerService.create(resource);
        } catch (ResourceAllreadyExistsException paee) {
            getLogger().warn("resource " + resource.toString() + " allready exists", paee);
            identifiant = paee.getResourceId();
            responseStatus = NO_CONTENT;
        } catch (Exception e) {
            handleException(e);
        }

        Response response = Response
                .status(responseStatus)
                .header("Location", baseURI + getRelativePath(resource) + "/" + identifiant)
                .build();

        return response;
    }

    public Response deleteResource(String resourceId) {

        try {
            resourceManagerService.delete(resourceId);
            return Response.status(OK).build();
        } catch (ResourceNotFoundException e) {
            getLogger().error("impossible to delete resource");
            throwWebApplicationException(NOT_FOUND, "resource_not_found", "the resource with id " + resourceId + " was not found");
        }

        return null;

    }


    private void checkNotNullResource(T resource) {
        if (resource == null) {
            getLogger().error("null resource cannot be create or update");

            throwWebApplicationException(Response.Status.BAD_REQUEST, "null_resource", "the resource cannot be null");
        }
    }


    public Collection<T> search(UriInfo uriInfo, HttpHeaders httpHeaders) {

        // get all query paramters
        //MultivaluedMap<String,String> queryParams = uriInfo.getQueryParameters(true);

        // get all Object Field to compare to query parameters
        Type sooper = getClass().getGenericSuperclass();
        Class t = (Class) ((ParameterizedType) sooper).getActualTypeArguments()[0];

        Collection<String> fields = new ArrayList<String>(t.getDeclaredFields().length);

        for (Field field : t.getDeclaredFields()) {
            fields.add(field.getName());
        }


        return null;
    }

    public Response getAll(UriInfo uriInfo) {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        Map<String, String> attributes = getAttributeMap(queryParameters);

        // build Accept-Range response header
        String acceptRangeHttpHeader = getAcceptRangeHeader();

        //check range format
        // manage range

        String range = queryParameters.getFirst("range");

        checkRange(range);

        //build Content-Range header
        List<T> entries = resourceManagerService.getAll(attributes);

        String contentRange = "0-" + (entries.size() - 1) + "/" + entries.size();

        Response.Status status = OK;

        String links = "";

        if (range != null) {

            int initialEntriesSize = entries.size();

            int startIndex = Integer.valueOf(range.split("-")[0]);
            int endIndex = Integer.valueOf(range.split("-")[1]);

            contentRange = startIndex + "-" + Math.min(endIndex, entries.size() - 1) + "/" + entries.size();

            if (endIndex - startIndex + 1 < entries.size()) {
                // set status partial if not all entries are returned
                status = PARTIAL_CONTENT;

                entries = entries.subList(startIndex, endIndex + 1);

                // adding link header

                List<Class> methodClass =
                        asList(GET.class, POST.class, PUT.class, PATCH.class, DELETE.class, HEAD.class, OPTIONS.class);

                List<String> methodClassName =
                        methodClass.stream().map(aClass -> aClass.getName()).collect(Collectors.toList());


                String httpMethod = "";

                for (Class clazz : this.getClass().getInterfaces()) {

                    Path path = (Path) clazz.getAnnotation(Path.class);
                    if ( path != null ){

                    }


                    try {
                        Method method = clazz.getDeclaredMethod("getAll", UriInfo.class);

                        List<String> methodNames = asList(method.getAnnotations()).stream()
                                .filter(annotation -> methodClassName.contains(annotation.annotationType().getName()))
                                .map(annotation1 -> annotation1.annotationType().getSimpleName())
                                .collect(Collectors.toList());

                        if ( !methodNames.isEmpty()) {
                            httpMethod = methodNames.get(0);
                        }




                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }


                links = getLinks(uriInfo, initialEntriesSize, startIndex, endIndex, httpMethod);

            }
        }



        // to remove
        Container container = new Container();
        for (T entrie : entries) {
            container.add((T) entrie);
        }


        return Response
                .status(status)
                //.entity(new Container((List<SampleData)> entries))
                .entity(container)
                .header("Link", links)
                .header("Accept-Range", acceptRangeHttpHeader)
                .header("Content-Range", contentRange).build();
    }

    String getLinks(UriInfo uriInfo, int initialEntriesSize, int startIndex, int endIndex, String httpMethod) {
        int rangeSize = endIndex - startIndex;

        int firstStartIndex = 0;
        int firstEndIndex = rangeSize;

        int prevStartIndex = Math.max(0, startIndex - rangeSize - 1);
        int prevEndIndex = prevStartIndex + rangeSize;

        int nextEndIndex = Math.min(startIndex + 2 * rangeSize, initialEntriesSize - 1);
        int nextStartIndex = nextEndIndex - rangeSize;


        int lastEndIndex = initialEntriesSize - 1;
        int lastStartIndex = lastEndIndex - rangeSize;

        List<Map<String,String>> links = Arrays.asList(
                getRangeMap(uriInfo, httpMethod, firstStartIndex, firstEndIndex, "first"),
                getRangeMap(uriInfo, httpMethod, prevStartIndex, prevEndIndex, "prev"),
                getRangeMap(uriInfo, httpMethod, nextStartIndex, nextEndIndex, "next"),
                getRangeMap(uriInfo, httpMethod, lastStartIndex, lastEndIndex, "last"));


        Function<Map.Entry<String,String>, String> entryWriter =  entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
        Function<Map<String,String>,String> mapWriter = map -> { return "{" + map.entrySet().stream().map(entryWriter).collect(Collectors.joining(",")) + "}";  };
        Function<List<Map<String,String>>, String> listWriter = list -> {

            return "["  +
                    list.stream()
                            .map(
                                    mapWriter
                            )
                            .collect(Collectors.joining(","))

                    + "]";
        };

        return listWriter.apply(links);

    }

    private Map<String, String> getRangeMap(UriInfo uriInfo, String httpMethod, int prevStartIndex, int prevEndIndex, String relation) {
        Map<String, String> prevRangeMap = new TreeMap<>();
        prevRangeMap.put("href",baseURI + uriInfo.getMatchedURIs().get(1) + "?range=" + prevStartIndex + "-" + prevEndIndex);
        prevRangeMap.put("method", httpMethod);
        prevRangeMap.put("rel", relation);

        return prevRangeMap;
    }

    private String getAcceptRangeHeader() {
        String acceptRangeHttpHeader = null;
        for (Class classInterface : this.getClass().getInterfaces()) {

            Path pathAnnotation = (Path) classInterface.getAnnotation(Path.class);
            if (pathAnnotation != null && !pathAnnotation.value().isEmpty()) {

                String resourcePathName = pathAnnotation.value().replace("/", "");
                // truncate la 's'
                if (resourcePathName.lastIndexOf("s") == resourcePathName.length() - 1) {
                    acceptRangeHttpHeader = resourcePathName.substring(0, resourcePathName.length() - 1) + " " + defaultRange;
                }


            }
        }
        return acceptRangeHttpHeader;
    }

    private Map<String, String> getAttributeMap(MultivaluedMap<String, String> queryParameters) {
        Map<String, String> attributes = new HashMap<>();

        Type sooper = getClass().getGenericSuperclass();
        Class t = (Class) ((ParameterizedType) sooper).getActualTypeArguments()[0];

        // build searchable map field from qury parameters
        for (Field field : t.getDeclaredFields()) {

            Seachable searchable = field.getAnnotation(Seachable.class);
            if (searchable != null) {

                String parameterName = searchable.value().isEmpty() ? field.getName() : searchable.value();
                if (queryParameters.containsKey(parameterName) &&
                        queryParameters.getFirst(parameterName) != null) {

                    attributes.put(field.getName(), queryParameters.getFirst(parameterName));
                }

            }

        }
        return attributes;
    }

    private void checkRange(String range) {
        Pattern rangePattern = Pattern.compile("[0-9]+-[0-9]+");

        if (range != null) {

            if (rangePattern.matcher(range).matches()) {
                int startIndex = Integer.valueOf(range.split("-")[0]);
                int endIndex = Integer.valueOf(range.split("-")[1]);

                if (endIndex - startIndex + 1 > defaultRange) {
                    throwWebApplicationException(BAD_REQUEST, "range_not_allowed", "the requested range is not allowed");
                }
            } else {
                throwWebApplicationException(BAD_REQUEST, "range_format_invalid", "the request range format is not valid");
            }
        }
    }

}

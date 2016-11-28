package fr.ratti.sample.api.rest.impl;

import fr.ratti.sample.api.exception.IllegalFormatException;
import fr.ratti.sample.api.exception.ResourceAllreadyExistsException;
import fr.ratti.sample.api.exception.ResourceNotFoundException;
import fr.ratti.sample.api.services.IDService;
import fr.ratti.sample.api.services.ResourceManagerService;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.easymock.Capture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.easymock.annotation.Mock;
import org.unitils.inject.annotation.InjectIntoByType;
import org.unitils.inject.annotation.TestedObject;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;
import uk.co.jemos.podam.api.PodamFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.easymock.CaptureType.ALL;
import static org.easymock.EasyMock.*;
import static org.unitils.easymock.EasyMockUnitils.replay;

/**
 * Created by bratti on 20/08/2016.
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@SpringApplicationContext("/fr/ratti/sample/api/rest/impl/AbstractSimpleResourceTest-context.xml")
public class AbstractSimpleResourceTest {


    @SpringBeanByType
    private PodamFactory factory;

    @TestedObject
    @SpringBeanByType
    private SampleResourceImpl sampleResource;

    @Mock
    @InjectIntoByType
    private ResourceManagerService<SampleData> sampleService;

    @Mock
    @InjectIntoByType
    private IDService idService;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private HttpHeaders httpHeaders;


    @SpringBeanByType
    WebClient webClient;


    @Test
    public void getResource_should_return_resource_when_resource_exists() throws Exception {

        String userId = UUID.randomUUID().toString();
        SampleData sampleObject = factory.manufacturePojo(SampleData.class);
        sampleObject.setId(userId);


        expect(uriInfo.getQueryParameters()).andStubReturn(new MultivaluedHashMap<>(0));
        expect(sampleService.get(userId)).andStubReturn(sampleObject);
        idService.validate(userId);

        replay();

        SampleData returnedSample = sampleResource.getResource(userId, uriInfo);

        assertThat(returnedSample).isEqualToComparingFieldByField(sampleObject);

    }

    @Test
    public void getResource_should_throw_HTTP_400_when_id_is_null() throws Exception {

        idService.validate(null);
        expectLastCall().andStubThrow(new IllegalFormatException());

        replay();

        try {
            sampleResource.getResource(null, uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }


    }

    @Test
    public void getResource_should_throw_HTTP_400_when_id_is_empty() throws Exception {

        idService.validate("");
        expectLastCall().andStubThrow(new IllegalFormatException());

        replay();

        try {
            sampleResource.getResource("", uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }

    }


    @Test
    public void getResource_should_throw_HTTP_500_when_service_throws_unexpected_exception() throws Exception {

        String userId = UUID.randomUUID().toString();

        idService.validate(anyString());
        expectLastCall().atLeastOnce();

        expect(sampleService.get(anyString())).andThrow(new RuntimeException());

        replay();

        try {
            sampleResource.getResource(userId, uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    @Test
    public void getResource_should_throw_HTTP_400_when_id_is_not_uuid() throws Exception {


        idService.validate(anyString());
        expectLastCall().andStubThrow(new IllegalFormatException());

        replay();


        try {
            sampleResource.getResource("4142331231312", uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }

    }

    @Test
    public void getResource_should_throw_HTTP_404_when_person_does_not_exist() throws Exception {

        String userId = UUID.randomUUID().toString();

        idService.validate(anyString());
        expectLastCall().anyTimes();

        expect(sampleService.get(userId)).andStubThrow(new ResourceNotFoundException());

        replay();

        try {
            sampleResource.getResource(userId, uriInfo);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        }

    }

    @Test
    public void getResource_should_return_selected_fields_when_httpQueryParams_specified_attributes_list() throws Exception {

        String sampleId = UUID.randomUUID().toString();


        ItemData item = new ItemData();
        item.setItem1("item::item1");
        item.setItem2("item::item2");

        SampleData sampleData = factory.manufacturePojo(SampleData.class);
        sampleData.setItemData(item);

        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<String, String>();
        queryParameters.add("fields", "mandatoryField,id");

        expect(uriInfo.getQueryParameters()).andStubReturn(queryParameters);
        expect(sampleService.get(sampleId)).andStubReturn(sampleData);

        idService.validate(sampleId);
        expectLastCall().anyTimes();

        replay();

        SampleData returnedSampleData1 = sampleResource.getResource(sampleId, uriInfo);

        assertThat(returnedSampleData1).isEqualToComparingOnlyGivenFields(sampleData, "mandatoryField", "id");

        assertThat(returnedSampleData1.getOptionalField()).isNull();
        assertThat(returnedSampleData1.getItemData()).isNull();

    }


    @Test
    public void getResource_should_return_selected_fields_when_httpQueryParams_specified_complex_attributes_list() throws Exception {

        String sampleId = UUID.randomUUID().toString();

        SubItemData subItemData = new SubItemData();
        subItemData.setSubItem1("subItemData::item1");
        subItemData.setSubItem2("subItemData::item2");

        ItemData item = new ItemData();
        item.setItem1("item::item1");
        item.setItem2("item::item2");
        item.setItem3("item::item3");
        item.setSubItemData(subItemData);

        SampleData sampleData = factory.manufacturePojo(SampleData.class);
        sampleData.setItemData(item);

        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<String, String>();
        queryParameters.add("fields", "mandatoryField,itemData(item1,item3),id");

        expect(uriInfo.getQueryParameters()).andStubReturn(queryParameters);
        expect(sampleService.get(sampleId)).andStubReturn(sampleData);

        idService.validate(sampleId);
        expectLastCall().anyTimes();

        replay();

        SampleData returnedSampleData1 = sampleResource.getResource(sampleId, uriInfo);

        assertThat(returnedSampleData1).isEqualToComparingOnlyGivenFields(sampleData, "mandatoryField", "id");

        assertThat(returnedSampleData1.getOptionalField()).isNull();
        assertThat(returnedSampleData1.getItemData()).isNotNull();

        assertThat(returnedSampleData1.getItemData()).isEqualToComparingOnlyGivenFields(item, "item1", "item3");
        assertThat(returnedSampleData1.getItemData().getItem2()).isNull();

        assertThat(returnedSampleData1.getItemData().getSubItemData()).isNull();
//        assertThat(returnedSampleData1.getItemData().getSubItemData()).isEqualToComparingOnlyGivenFields("subItem1");
//        assertThat(returnedSampleData1.getItemData().getSubItemData().getSubItem2()).isNull();


    }


    @Test
    public void createResource_should_return_HTTP_201_when_sampleObject_is_correctly_created() throws Exception {

        String sampleId = UUID.randomUUID().toString();
        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        expect(sampleService.create(sampleData)).andStubReturn(sampleId);

        replay();

        Response response = sampleResource.createResource(sampleData);

        assertThat(response.getStatus()).isEqualTo(CREATED.getStatusCode());
        assertThat(response.getHeaderString("Location"))
                .isNotNull()
                .isEqualTo("http://api.url.com/v1/api/samples/" + sampleId);

    }

    @Test
    public void createResource_should_return_HTTP_400_when_person_is_null() throws Exception {
        try {
            sampleResource.createResource(null);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }

    }


    @Test
    public void createResource_should_return_HTTP_400_when_sampleData_has_not_mandatory_field() throws Exception {

        try {
            SampleData sampleData = factory.manufacturePojo(SampleData.class);
            sampleData.setMandatoryField(null);

            sampleResource.createResource(sampleData);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void createResource_should_return_HTTP_204_when_person_allready_exists() throws Exception {

        String sampleId = UUID.randomUUID().toString();
        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        expect(sampleService.create(sampleData)).andStubThrow(new ResourceAllreadyExistsException(sampleId));

        replay();

        Response response = sampleResource.createResource(sampleData);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        assertThat(response.getHeaderString("Location"))
                .isNotNull()
                .isEqualTo("http://api.url.com/v1/api/samples/" + sampleId);

    }


    @Test
    public void search_should_return_all_data_when_service_return_collection() throws Exception {

        sampleResource.search(null, null);

    }


    @Test
    public void update_should_update_data_when_data_exists() throws Exception {

        String sampleId = UUID.randomUUID().toString();
        SampleData sampleData = factory.manufacturePojo(SampleData.class);
        SampleData updateSampleData = factory.manufacturePojo(SampleData.class);

        Capture<SampleData> capture = Capture.newInstance(ALL);

        expect(sampleService.get(sampleId)).andReturn(sampleData);
        sampleService.update(eq(sampleId), capture(capture));
        expectLastCall().atLeastOnce();

        idService.validate(sampleId);
        expectLastCall().anyTimes();

        replay();

        Response response = sampleResource.updateResource(sampleId, updateSampleData);

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

        assertThat(capture.hasCaptured()).isTrue();
        assertThat(capture.getValues()).hasSize(1);
        assertThat(capture.getValue()).isEqualToComparingFieldByField(updateSampleData);

    }

    @Test
    public void update_should_return_HTTP_400_when_mandatory_fields_are_not_set() throws Exception {

        String sampleId = UUID.randomUUID().toString();
        SampleData sampleData = factory.manufacturePojo(SampleData.class);
        sampleData.setMandatoryField(null);

        idService.validate(sampleId);
        expectLastCall().anyTimes();

        replay();


        try {
            sampleResource.updateResource(sampleId, sampleData);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }


    }


    @Test
    public void update_should_return_HTTP_400_when_resource_is_null() throws Exception {

        String sampleId = UUID.randomUUID().toString();

        idService.validate(sampleId);
        expectLastCall().anyTimes();

        replay();

        try {
            sampleResource.updateResource(sampleId, null);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }
    }


    @Test
    public void update_should_return_HTTP_400_when_id_is_null() throws Exception {

        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        try {
            sampleResource.updateResource(null, sampleData);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
            assertThat((Map<String, String>) wae.getResponse().getEntity()).containsEntry("error_code", "null_id");
        }

    }


    @Test
    public void update_should_return_HTTP_400_when_id_format_is_wrong() throws Exception {

        String sampleId = "23463212343524232";
        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        idService.validate(sampleId);
        expectLastCall().andStubThrow(new IllegalFormatException());

        replay();

        try {
            sampleResource.updateResource(sampleId, sampleData);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void update_should_return_HTTP_201_and_create_resource_when_resource_does_not_exist() throws Exception {

        String sampleId = "23463212343524232";
        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        idService.validate(sampleId);
        expectLastCall().anyTimes();

        expect(sampleService.get(sampleId)).andStubThrow(new ResourceNotFoundException());
        sampleService.create(eq(sampleId), eq(sampleData));

        replay();

        Response response = sampleResource.updateResource(sampleId, sampleData);

        assertThat(response.getStatus()).isEqualTo(CREATED.getStatusCode());
        assertThat(response.getHeaderString("Location"))
                .isNotNull()
                .isEqualTo("http://api.url.com/v1/api/samples/" + sampleId);

    }


    @Test
    public void update_should_return_HTTP_500_and_create_resource_when_resource_does_not_exist_and_service_throw_unexpected_exception() throws Exception {

        String sampleId = "23463212343524232";
        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        idService.validate(sampleId);
        expectLastCall().anyTimes();

        expect(sampleService.get(sampleId)).andStubThrow(new ResourceNotFoundException());
        sampleService.create(eq(sampleId), eq(sampleData));
        expectLastCall().andStubThrow(new RuntimeException());

        replay();

        try {
            sampleResource.updateResource(sampleId, sampleData);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    @Test
    public void update_should_return_HTTP_500_and_create_resource_when_resource_exists_and_service_throw_unexpected_exception() throws Exception {

        String sampleId = "23463212343524232";
        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        idService.validate(sampleId);
        expectLastCall().anyTimes();

        expect(sampleService.get(sampleId)).andStubReturn(sampleData);
        sampleService.update(eq(sampleId), anyObject(SampleData.class));
        expectLastCall().andStubThrow(new RuntimeException());

        replay();

        try {
            sampleResource.updateResource(sampleId, sampleData);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @Test
    public void update_should_return_HTTP_400_and_create_resource_when_resource_does_not_exist_and_service_throw_expected_exception() throws Exception {

        String sampleId = "23463212343524232";
        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        idService.validate(sampleId);
        expectLastCall().anyTimes();

        expect(sampleService.get(sampleId)).andStubThrow(new ResourceNotFoundException());
        sampleService.create(eq(sampleId), eq(sampleData));
        expectLastCall().andStubThrow(new ExpectedException());

        replay();


        Response response = sampleResource.updateResource(sampleId, sampleData);

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

    }


    @Test
    public void update_should_return_HTTP_400_and_create_resource_when_resource_exists_and_service_throw_expected_exception() throws Exception {

        String sampleId = "23463212343524232";
        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        idService.validate(sampleId);
        expectLastCall().anyTimes();

        expect(sampleService.get(sampleId)).andStubReturn(sampleData);
        sampleService.update(eq(sampleId), eq(sampleData));
        expectLastCall().andStubThrow(new ExpectedException());

        replay();

        Response response = sampleResource.updateResource(sampleId, sampleData);

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

    }


    @Test
    public void patch_should_update_resource_when_resource_exists() throws Exception {


        String sampleId = "23463212343524232";
        SampleData sampleData = factory.manufacturePojo(SampleData.class);
        sampleData.setId("sampleData::Id");
        sampleData.setMandatoryField("sampleData::mandotoryField");
        sampleData.setOptionalField("sampleData::optionalField");

        SampleData updatedSampleData = factory.manufacturePojo(SampleData.class);
        updatedSampleData.setMandatoryField(null);

        updatedSampleData.setId("updatedSampleData::Id");
        updatedSampleData.setOptionalField("updatedSampleData::optionalField");

        idService.validate(sampleId);

        Capture<SampleData> sampleDataCapture = Capture.newInstance(ALL);

        expect(sampleService.get(sampleId)).andStubReturn(sampleData);
        sampleService.update(eq(sampleId), capture(sampleDataCapture));
        expectLastCall().atLeastOnce();

        replay();

        Response response = sampleResource.patchResource(sampleId, updatedSampleData);

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

        assertThat(sampleDataCapture.hasCaptured()).isTrue();
        assertThat(sampleDataCapture.getValues()).hasSize(1);

        assertThat(sampleDataCapture.getValue().getId()).isEqualTo(updatedSampleData.getId());
        assertThat(sampleDataCapture.getValue().getOptionalField()).isEqualTo(updatedSampleData.getOptionalField());
        assertThat(sampleDataCapture.getValue().getMandatoryField()).isEqualTo(sampleData.getMandatoryField());

    }


    @Test
    public void patch_should_return_HTTP_404_when_resource_does_not_exist() throws Exception {


        String sampleId = "23463212343524232";

        SampleData updatedSampleData = factory.manufacturePojo(SampleData.class);

        idService.validate(sampleId);
        expect(sampleService.get(sampleId)).andStubThrow(new ResourceNotFoundException());

        replay();

        try {
            sampleResource.patchResource(sampleId, updatedSampleData);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        }

    }


    @Test
    public void patch_should_return_HTTP_400_when_id_is_null() throws Exception {


        SampleData updatedSampleData = factory.manufacturePojo(SampleData.class);

        replay();

        try {
            sampleResource.patchResource(null, updatedSampleData);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }

    }

    @Test
    public void patch_should_return_HTTP_400_when_id_format_is_wrong() throws Exception {

        SampleData updatedSampleData = factory.manufacturePojo(SampleData.class);

        idService.validate(anyString());
        expectLastCall().andStubThrow(new IllegalFormatException());

        replay();

        try {
            sampleResource.patchResource("43242342342342", updatedSampleData);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }
    }


    @Test
    public void delete_should_delete_resource_when_resource_exists() throws Exception {

        String sampleId = UUID.randomUUID().toString();
        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        sampleService.delete(sampleId);

        replay();

        Response response = sampleResource.deleteResource(sampleId);

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

    }


    @Test
    public void delete_should_return_HTTP_404_when_resource_does_not_exist() throws Exception {

        String sampleId = UUID.randomUUID().toString();
        SampleData sampleData = factory.manufacturePojo(SampleData.class);

        sampleService.delete(sampleId);
        expectLastCall().andStubThrow(new ResourceNotFoundException());

        replay();

        try {
            sampleResource.deleteResource(sampleId);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        }

    }


    @Test
    public void getAll_should_call_services_with_fieldname__of_searchable_fields_when_searchable_annotation_value_is_empty() throws Exception {


        SampleData sampleData1 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData2 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData3 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData4 = factory.manufacturePojo(SampleData.class);

        List<SampleData> sampleDatas = Arrays.asList(sampleData1, sampleData2, sampleData3, sampleData4);
        MultivaluedMap<String, String> attributes = new MultivaluedHashMap<String, String>(2);
        attributes.putSingle("id", "myId");
        attributes.putSingle("mandatoryField", "myMandatoryFieldValue");

        Capture<Map<String, String>> attributesCapture = Capture.newInstance(ALL);

        expect(uriInfo.getQueryParameters()).andStubReturn(attributes);
        expect(sampleService.getAll(capture(attributesCapture))).andReturn(sampleDatas).atLeastOnce();

        replay();

        Response response = sampleResource.getAll(uriInfo);

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(attributesCapture.hasCaptured()).isTrue();
        assertThat(attributesCapture.getValues()).hasSize(1);
        assertThat(attributesCapture.getValue()).hasSize(1);
        assertThat(attributesCapture.getValue()).containsEntry("id", "myId");

    }


    @Test
    public void getAll_should_call_services_with_annotation_value_of_searchable_fields_when_searchable_annotation_value_is_not_empty() throws Exception {


        SampleData sampleData1 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData2 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData3 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData4 = factory.manufacturePojo(SampleData.class);

        List<SampleData> sampleDatas = Arrays.asList(sampleData1, sampleData2, sampleData3, sampleData4);
        MultivaluedMap<String, String> attributes = new MultivaluedHashMap<>(2);
        // id field an empty value for @Searchable annotation
        attributes.putSingle("id", "myId");
        // optionalField has value "name" for @Searchable annotation
        attributes.putSingle("name", "myOptionalFieldValue");

        Capture<Map<String, String>> attributesCapture = Capture.newInstance(ALL);

        expect(uriInfo.getQueryParameters()).andStubReturn(attributes);
        expect(sampleService.getAll(capture(attributesCapture))).andReturn(sampleDatas).atLeastOnce();

        replay();

        Response response = sampleResource.getAll(uriInfo);

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(attributesCapture.hasCaptured()).isTrue();
        assertThat(attributesCapture.getValues()).hasSize(1);
        assertThat(attributesCapture.getValue()).hasSize(2);
        assertThat(attributesCapture.getValue()).containsEntry("id", "myId");
        assertThat(attributesCapture.getValue()).containsEntry("optionalField", "myOptionalFieldValue");
    }


    @Test
    public void getAll_should_set_default_AcceptRange_header_on_response_when_range_is_not_set() throws Exception {


        SampleData sampleData1 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData2 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData3 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData4 = factory.manufacturePojo(SampleData.class);

        List<SampleData> sampleDatas = Arrays.asList(sampleData1, sampleData2, sampleData3, sampleData4);
        MultivaluedMap<String, String> attributes = new MultivaluedHashMap<String, String>(2);
        attributes.putSingle("range", "0-99");


        Capture<Map<String, String>> attributesCapture = Capture.newInstance(ALL);

        expect(uriInfo.getQueryParameters()).andStubReturn(attributes);
        expect(sampleService.getAll(anyObject(Map.class))).andReturn(sampleDatas).atLeastOnce();

        replay();

        Response response = sampleResource.getAll(uriInfo);

        assertThat(response.getHeaderString("Accept-Range")).isEqualTo("sample 100");

    }


    @Test
    public void getAll_should_return_http_206_on_response_when_not_all_entries_are_returned() throws Exception {


        SampleData sampleData1 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData2 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData3 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData4 = factory.manufacturePojo(SampleData.class);

        sampleData1.setId("sampleData1:;id");
        sampleData2.setId("sampleData2:;id");
        sampleData3.setId("sampleData3:;id");
        sampleData4.setId("sampleData4:;id");

        List<SampleData> sampleDatas = Arrays.asList(sampleData1, sampleData2, sampleData3, sampleData4);
        MultivaluedMap<String, String> attributes = new MultivaluedHashMap<String, String>(2);
        attributes.putSingle("range", "0-2");


        expect(uriInfo.getQueryParameters()).andStubReturn(attributes);
        expect(sampleService.getAll(anyObject(Map.class))).andReturn(sampleDatas).atLeastOnce();
        expect(uriInfo.getMatchedURIs()).andStubReturn(Arrays.asList("/samples/","/samples"));


        replay();

        Response response = sampleResource.getAll(uriInfo);

        assertThat(response.getHeaderString("Content-Range")).isEqualTo("0-2/4");
        assertThat(response.getStatus()).isEqualTo(Response.Status.PARTIAL_CONTENT.getStatusCode());

        assertThat((Collection<SampleData>) response.getEntity()).hasSize(3);
        assertThat((Collection<SampleData>) response.getEntity()).containsExactly(sampleData1, sampleData2, sampleData3);


    }


    @Test
    public void getAll_should_return_http_200_on_response_when_all_entries_are_returned() throws Exception {


        SampleData sampleData1 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData2 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData3 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData4 = factory.manufacturePojo(SampleData.class);

        sampleData1.setId("sampleData1:;id");
        sampleData2.setId("sampleData2:;id");
        sampleData3.setId("sampleData3:;id");
        sampleData4.setId("sampleData4:;id");

        List<SampleData> sampleDatas = Arrays.asList(sampleData1, sampleData2, sampleData3, sampleData4);
        MultivaluedMap<String, String> attributes = new MultivaluedHashMap<String, String>(2);
        attributes.putSingle("range", "0-10");


        expect(uriInfo.getQueryParameters()).andStubReturn(attributes);
        expect(sampleService.getAll(anyObject(Map.class))).andReturn(sampleDatas).atLeastOnce();

        replay();

        Response response = sampleResource.getAll(uriInfo);

        assertThat(response.getHeaderString("Content-Range")).isEqualTo("0-3/4");
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        assertThat((Collection<SampleData>) response.getEntity()).hasSize(4);
        assertThat((Collection<SampleData>) response.getEntity()).containsExactly(sampleData1, sampleData2, sampleData3, sampleData4);

    }


    @Test
    public void getAll_should_return_http_400_on_response_when_range_parameter_has_wrong_format() throws Exception {


        SampleData sampleData1 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData2 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData3 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData4 = factory.manufacturePojo(SampleData.class);

        sampleData1.setId("sampleData1:;id");
        sampleData2.setId("sampleData2:;id");
        sampleData3.setId("sampleData3:;id");
        sampleData4.setId("sampleData4:;id");

        List<SampleData> sampleDatas = Arrays.asList(sampleData1, sampleData2, sampleData3, sampleData4);
        MultivaluedMap<String, String> attributes = new MultivaluedHashMap<String, String>(2);
        attributes.putSingle("range", "5");


        expect(uriInfo.getQueryParameters()).andStubReturn(attributes);

        replay();

        try {
            sampleResource.getAll(uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }

    }


    @Test
    public void getAll_should_return_http_400_on_response_when_range_is_greater_than_allowed_range() throws Exception {


        SampleData sampleData1 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData2 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData3 = factory.manufacturePojo(SampleData.class);
        SampleData sampleData4 = factory.manufacturePojo(SampleData.class);

        sampleData1.setId("sampleData1:;id");
        sampleData2.setId("sampleData2:;id");
        sampleData3.setId("sampleData3:;id");
        sampleData4.setId("sampleData4:;id");

        MultivaluedMap<String, String> attributes = new MultivaluedHashMap<String, String>(2);
        attributes.putSingle("range", "0-200");


        expect(uriInfo.getQueryParameters()).andStubReturn(attributes);

        replay();

        try {
            sampleResource.getAll(uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }

    }


    @Test
    public void getAll_should_return_links_when_response_is_partial() throws Exception {

        List<SampleData> datas = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            datas.add(factory.manufacturePojo(SampleData.class));
        }


        MultivaluedMap<String, String> attributes = new MultivaluedHashMap<String, String>(2);
        attributes.putSingle("range", "5-14");

        expect(uriInfo.getQueryParameters()).andStubReturn(attributes);
        expect(uriInfo.getMatchedURIs()).andStubReturn(Arrays.asList("/samples/","/samples"));
        expect(sampleService.getAll(anyObject(Map.class))).andStubReturn(datas);

        replay();

        Response response = sampleResource.getAll(uriInfo);

        assertThat(response.getStatus()).isEqualTo(PARTIAL_CONTENT.getStatusCode());


        System.out.println(response.getHeaderString("Link"));
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> returnedLinks = new ArrayList<Map<String,String>>(0);
        returnedLinks = mapper.readValue(response.getHeaderString("Link"), returnedLinks.getClass());


        //List<Map<String, String>> returnedLinks = (List<Map<String, String>>) response.getHeaders().getFirst("Link");

        assertThat(returnedLinks).hasSize(4);

        assertThat(returnedLinks.get(0)).containsEntry("rel", "first");
        assertThat(returnedLinks.get(1)).containsEntry("rel", "prev");
        assertThat(returnedLinks.get(2)).containsEntry("rel", "next");
        assertThat(returnedLinks.get(3)).containsEntry("rel", "last");

        assertThat(returnedLinks.get(0)).containsEntry("method", "GET");
        assertThat(returnedLinks.get(1)).containsEntry("method", "GET");
        assertThat(returnedLinks.get(2)).containsEntry("method", "GET");
        assertThat(returnedLinks.get(3)).containsEntry("method", "GET");

        assertThat(returnedLinks.get(0)).containsEntry("method", "GET");
        assertThat(returnedLinks.get(1)).containsEntry("method", "GET");
        assertThat(returnedLinks.get(2)).containsEntry("method", "GET");
        assertThat(returnedLinks.get(3)).containsEntry("method", "GET");

        assertThat(returnedLinks.get(0))
                .containsEntry("href", "http://api.url.com/v1/api/samples?range=0-9");
        assertThat(returnedLinks.get(1))
                .containsEntry("href", "http://api.url.com/v1/api/samples?range=0-9");
        assertThat(returnedLinks.get(2))
                .containsEntry("href", "http://api.url.com/v1/api/samples?range=10-19");
        assertThat(returnedLinks.get(3))
                .containsEntry("href", "http://api.url.com/v1/api/samples?range=10-19");


    }

    @Test
    public void test_stream() {

        List<String> list = Arrays.asList("prenom,nom,tel(fixe)", "age,adresse(rue)");

        List<String> result = list.stream()
                .map(str -> Arrays.asList(str.split(",")))
                .flatMap(l -> l.stream())
                .collect(Collectors.toList());

//        result.stream().forEach(System.out::println);

//        Pattern pattern = Pattern.compile("[^(]+\\([^)]+\\)");

        String fun = "[^(]+\\([^)]+\\)";
        String word = "[^(),]+";

        String attribute = "(" + fun + "|" + word + ")";


        String attributes = attribute + "(," + attribute + ")*";

        Pattern funPattern = Pattern.compile(fun);
        Pattern wordPattern = Pattern.compile(word);

        Pattern pattern = Pattern.compile(attribute);


        Pattern fieldPattern = Pattern.compile("[^(,]+\\([^()]+\\)|[^(),]+|[^(,]+\\(.+?\\)");
//        Pattern fieldPattern = Pattern.compile("[^(,]+\\([^()]+?\\)|[^(),]+");
//        Pattern fieldPattern = Pattern.compile("[^(,]+\\(.+\\)|[^(),]+");

//        Matcher complexAttrMatcher = fieldPattern.matcher("prenom,nom,tel(fixe),age,adresse(rue,ville(maire)),mesuration(taille)");
        Matcher complexAttrMatcher = fieldPattern.matcher("prenom,nom,tel(fixe),age,adresse(rue,ville),mesuration(taille)");
        while (complexAttrMatcher.find()) {
            System.out.println("groupe = " + complexAttrMatcher.group());
        }

        String field = "adresse(rue,ville)";
        String fieldName = field.substring(0, field.indexOf("("));
        String parameters = field.substring(field.indexOf("(") + 1, field.indexOf(")"));
        System.out.println(fieldName);
        System.out.println(parameters);


        // on recup�re les attributes complexes
//        wantedfields.stream()
//                .filter(str -> complexAttribute.matcher(str).matches())
//                .collect(Collectors.toMap(
//
//
//                )) ;




/*

        assertThat(funPattern.matcher("adresse(rue)").matches()).isTrue();
        assertThat(wordPattern.matcher("adresse").matches()).isTrue();

        assertThat(pattern.matcher("adresse(rue)").matches()).isTrue();
        assertThat(attributesPattern.matcher("prenom,nom,tel(fixe)").matches()).isTrue();

//        Map<String,String> complexAttributes =
                result.stream()
                        .filter(str -> funPattern.matcher(str).matches())
                        .forEach(System.out::println);

        System.out.println(attributesPattern.matcher("prenom,nom,tel(fixe)").groupCount());

*/


    }


    @Test
    public void test_webclient() throws Exception {

        String sampleId = UUID.randomUUID().toString();

        List<SampleData> datas = new ArrayList<>(200);
        for (int i = 0; i < 200; i++) {
            SampleData sampleData = factory.manufacturePojo(SampleData.class);
            sampleData.setId("id-" + i);
            datas.add(sampleData);
        }



        MultivaluedMap<String, String> attributes = new MultivaluedHashMap<String, String>(2);
        attributes.putSingle("range", "10-19");

        idService.validate(sampleId);
        expectLastCall().anyTimes();
        expect(sampleService.get(sampleId)).andStubReturn(datas.get(0));

        expect(sampleService.getAll(anyObject(Map.class))).andStubReturn(datas);
        webClient.reset();

        webClient.accept(APPLICATION_JSON);
        webClient.header("Content-Type", APPLICATION_JSON);
        webClient.path("samples");
        //webClient.path(sampleId);
        webClient.query("range", "10-19");
        webClient.query("fields","id,itemData(item1)");


        replay();


        Response response = webClient.get();

        System.out.println(IOUtils.toString((InputStream) response.getEntity()));



        response.getHeaders().entrySet().stream()
                .forEach(entry -> System.out.println(entry.getKey() + ":" + entry.getValue() ));


        assertThat(response.getStatus()).isEqualTo(PARTIAL_CONTENT.getStatusCode());


    }


    @Test
    public void test_printmap() throws Exception  {

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

        List<Map<String,String>> list = new ArrayList<>(2);

        for (int i = 0; i < 2 ; i++) {

            Map<String, String> map = new HashMap<>(4);
            map.put("m" + i + "-key-1", "value-1");
            map.put("m" + i + "-key-2", "value-2");
            map.put("m" + i + "-key-3", "value-3");
            map.put("m" + i + "-key-4", "value-3");

            list.add(map);





        }


        System.out.println(listWriter.apply(list));


    }



}

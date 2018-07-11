/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.lib.elastic.http.request;
import com.esri.geoportal.base.util.JsonUtil;
import com.esri.geoportal.context.AppResponse;
import com.esri.geoportal.lib.elastic.http.AccessUtil;
import com.esri.geoportal.lib.elastic.http.ElasticClient;

import java.io.FileNotFoundException;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Get the metadata for an item.
 */
public class GetMetadataRequest extends com.esri.geoportal.lib.elastic.request.GetMetadataRequest {
  
  /** Instance variables. */
  private String id;
  
  /** Constructor. */
  public GetMetadataRequest() {
    super();
  }
  
  /** The item id. */
  public String getId() {
    return id;
  }
  /** The item id. */
  public void setId(String id) {
    this.id = id;
  }

  /** Methods =============================================================== */

  @Override
  public AppResponse execute() throws Exception {    
    System.out.println("com.esri.geoportal.lib.elastic.http.request.GetMetadataRequest"); // TODO temporary
    AppResponse response = new AppResponse();
    String id = getId();
    if (id == null || id.length() == 0) {
      response.writeIdIsMissing(this);
      return response;
    }
    AccessUtil au = new AccessUtil();
    id = au.determineId(id); // TODO
    au.ensureReadAccess(getUser(),id); // TODO
    
    ElasticClient client = ElasticClient.newClient();
    boolean useSeparateXmlItem = client.useSeparateXmlItem();
    String url = null, xmlField = null;
    if (useSeparateXmlItem) {
      xmlField = "sys_clob";
      url = client.getXmlUrl(id);
    } else {
      xmlField = "xml";
      url = client.getItemUrl(id);
    }
    
    try {
      String result = client.sendGet(url);
      if (result != null && result.length() > 0) {
        JsonObject jso = (JsonObject)JsonUtil.toJsonStructure(result);
        JsonObject source = jso.getJsonObject("_source");
        if (source.containsKey(xmlField)) {
          String xml = source.getString(xmlField);
          //System.out.println("xml:\r\n"+xml); // TODO
          this.writeOk(response,xml);
        } else {
          this.writeOk(response,null); // TODO return "", null or an error?
        }
      } else {
        response.writeIdNotFound(this,id);
      }
    } catch (FileNotFoundException e) {
      response.writeIdNotFound(this,id);
    }
    return response;
  }
  
  /**
   * Initialize.
   * @param id the item id
   */
  public void init(String id) {
    this.setId(id);
  }
  
  /**
   * Write the response. 
   * @param response the response
   * @param xml the metadata xml
   */
  public void writeOk(AppResponse response, String xml) {
    response.setEntity(xml);
    response.setMediaType(MediaType.APPLICATION_XML_TYPE.withCharset("UTF-8"));
    response.setStatus(Response.Status.OK);
  }

}

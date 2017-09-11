package org.idstack.extractor.api;

import org.idstack.feature.Constant;
import org.idstack.feature.FeatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Chanaka Lakmal
 * @date 31/8/2017
 * @since 1.0
 */

@RestController
public class APIHandler {

    @Autowired
    Router router;

    @RequestMapping("/")
    public void root(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendRedirect("http://idstack.one/extractor");
    }

    @RequestMapping(value = "/{version}/{apikey}/saveconfig/{type}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String saveBasicConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @RequestBody String json) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        switch (type) {
            case Constant.Configuration.BASIC_CONFIG:
                return FeatureImpl.getFactory().saveBasicConfiguration(router.configFilePath, json);
            case Constant.Configuration.DOCUMENT_CONFIG:
                return FeatureImpl.getFactory().saveDocumentConfiguration(router.configFilePath, json);
            default:
                return Constant.Status.ERROR_REQUEST;
        }
    }

    @RequestMapping(value = {"/{version}/{apikey}/getconfig/{type}/{property}", "/{version}/{apikey}/getconfig/{type}/"}, method = RequestMethod.GET)
    @ResponseBody
    public String getConfiguration(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @PathVariable("type") String type, @PathVariable("property") Optional<String> property) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().getConfigurationAsJson(router.configFilePath, router.getConfigFileName(type), property);
    }

    @RequestMapping(value = "/{version}/{apikey}/savepubcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePublicCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().savePublicCertificate(certificate, router.configFilePath, router.pubCertFilePath, router.pubCertType);
    }

    @RequestMapping(value = "/{version}/getpubcert", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public String getPublicCertificate(@PathVariable("version") String version) {
        if (!FeatureImpl.getFactory().validateRequest(version)) return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().getPublicCertificateURL(router.configFilePath, router.pubCertFilePath, router.pubCertType);
    }

    @RequestMapping(value = "/{version}/{apikey}/savepvtcert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String savePrivateCertificate(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestParam(value = "cert") final MultipartFile certificate, @RequestParam(value = "password") String password) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return FeatureImpl.getFactory().savePrivateCertificate(certificate, password, router.configFilePath, router.pvtCertFilePath, router.pvtCertType, router.pvtCertPasswordType);
    }

    // TODO: request for a scanned PDF
    @RequestMapping(value = "/{version}/{apikey}/extract", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String createMR(@PathVariable("version") String version, @PathVariable("apikey") String apikey, @RequestBody String json, @RequestParam(value = "pdf") final File pdf) {
        if (!FeatureImpl.getFactory().validateRequest(version, router.apiKey, apikey))
            return Constant.Status.ERROR_REQUEST;
        return router.createMR(json, pdf);
    }
}
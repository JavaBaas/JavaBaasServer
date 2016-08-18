package com.javabaas.server;

import com.javabaas.server.admin.entity.App;
import com.javabaas.server.file.entity.FileStoragePlatform;
import com.javabaas.server.admin.service.AppService;
import com.javabaas.server.common.util.JSONUtil;
import com.javabaas.server.file.entity.BaasFile;
import com.javabaas.server.file.service.FileService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Created by Staryet on 15/8/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest("server.port:9000")
public class FileTests {

    @Autowired
    private FileService fileService;
    @Autowired
    private AppService appService;
    private FileStoragePlatform platform = FileStoragePlatform.Test;
    @Autowired
    private JSONUtil jsonUtil;
    private App app;

    @Before
    public void before() {
        app = new App();
        app.setName("ObjectTestApp");
        appService.insert(app);
    }

    @After
    public void after() {
        appService.delete(app.getId());
    }

    @Test
    public void testInsert() {
        BaasFile file = new BaasFile();
        file.setName("baidu");
        String url = "https://ss0.bdstatic.com/5aV1bjqh_Q23odCf/static/superplus/img/logo_white_ee663702.png";
        file.setUrl(url);
        BaasFile baasFile = fileService.saveFile(app.getId(), "admin", file);

        file = fileService.getFile(app.getId(), "admin", baasFile.getId());
        Assert.assertThat(file.getName(), equalTo("baidu"));
        Assert.assertThat(file.getUrl(), equalTo(url));
        Assert.assertThat(file.getId(), not(nullValue()));
    }

    @Test
    public void testUpload() {
        //模拟上传回调操作
        Map<String, String> callbackParams = new HashMap<>();
        callbackParams.put("name", "fileTest");
        callbackParams.put("app", app.getId());
        callbackParams.put("plat", "admin");
        String fileId = fileService.callback(FileStoragePlatform.Test, jsonUtil.writeValueAsString(callbackParams), null).getId();

        //检查文件是否保存成功
        BaasFile file = fileService.getFile(app.getId(), "admin", fileId);
        Assert.assertThat(file.getId(), equalTo(fileId));
    }

    @Test
    public void testFetch() {
        BaasFile file = new BaasFile();
        file.setName("file");
        file.setUrl("http://7xnus0.com2.z0.glb.qiniucdn.com/56308742a290aa5006f2b0d6/83debfaee6b040e6bbdd1fdb3e11a84d");
        String fileId = fileService.saveFileWithFetch(app.getId(), "admin", platform, file, null).getId();
        //检查文件是否保存成功
        file = fileService.getFile(app.getId(), "admin", fileId);
        Assert.assertThat(file.getId(), equalTo(fileId));
    }

}

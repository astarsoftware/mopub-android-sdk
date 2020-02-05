// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import com.mopub.common.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static com.mopub.mobileads.VastResourceTwo.fromVastResourceXmlManager;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class VastResourceTwoTest {

    private VastResourceTwo subject;
    @Mock private VastWebView mockVastWebView;

    @Before
    public void setup() {
        subject = new VastResourceTwo("resource", VastResourceTwo.Type.STATIC_RESOURCE,
                VastResourceTwo.CreativeType.IMAGE, 50, 100);
    }

    @Test
    public void fromVastResourceXmlManager_withIFrameType_shouldSetIFrameResource() throws Exception {
        final VastResourceXmlManager resourceXmlManager =
                VastXmlManagerAggregatorTest.initializeVastResourceXmlManagerMock(
                        "StaticResource", "image/jpeg", "IFrameResource", "HTMLResource");

        VastResourceTwo vastResource =
                fromVastResourceXmlManager(resourceXmlManager, VastResourceTwo.Type.IFRAME_RESOURCE,
                        50, 100);

        assertThat(vastResource.getResource()).isEqualTo("IFrameResource");
        assertThat(vastResource.getType())
                .isEqualTo(VastResourceTwo.Type.IFRAME_RESOURCE);
        assertThat(vastResource.getCreativeType())
                .isEqualTo(VastResourceTwo.CreativeType.NONE);
    }

    @Test
    public void fromVastResourceXmlManager_withHTMLType_shouldSetHTMLResource() throws Exception {
        final VastResourceXmlManager resourceXmlManager =
                VastXmlManagerAggregatorTest.initializeVastResourceXmlManagerMock(
                        "StaticResource", "image/jpeg", "IFrameResource", "HTMLResource");

        VastResourceTwo vastResource =
                fromVastResourceXmlManager(resourceXmlManager, VastResourceTwo.Type.HTML_RESOURCE, 50,
                        100);

        assertThat(vastResource.getResource()).isEqualTo("HTMLResource");
        assertThat(vastResource.getType())
                .isEqualTo(VastResourceTwo.Type.HTML_RESOURCE);
        assertThat(vastResource.getCreativeType())
                .isEqualTo(VastResourceTwo.CreativeType.NONE);
    }

    @Test
    public void fromVastResourceXmlManager_withStaticType_withImageCreativeType_shouldSetImageCreativeType() throws Exception {
        final VastResourceXmlManager resourceXmlManager =
                VastXmlManagerAggregatorTest.initializeVastResourceXmlManagerMock(
                        "StaticResource", "image/jpeg", "IFrameResource", "HTMLResource");

        VastResourceTwo vastResource = fromVastResourceXmlManager(resourceXmlManager,
                VastResourceTwo.Type.STATIC_RESOURCE, 50, 100);

        assertThat(vastResource.getResource()).isEqualTo("StaticResource");
        assertThat(vastResource.getType())
                .isEqualTo(VastResourceTwo.Type.STATIC_RESOURCE);
        assertThat(vastResource.getCreativeType())
                .isEqualTo(VastResourceTwo.CreativeType.IMAGE);
    }

    @Test
    public void fromVastResourceXmlManager_withStaticType_withJavaScriptCreativeType_shouldSetJavascriptCreativeType() throws Exception {
        final VastResourceXmlManager resourceXmlManager =
                VastXmlManagerAggregatorTest.initializeVastResourceXmlManagerMock(
                        "StaticResource", "application/x-javascript", "IFrameResource", "HTMLResource");

        VastResourceTwo vastResource = fromVastResourceXmlManager(resourceXmlManager,
                VastResourceTwo.Type.STATIC_RESOURCE, 50, 100);

        assertThat(vastResource.getResource()).isEqualTo("StaticResource");
        assertThat(vastResource.getType())
                .isEqualTo(VastResourceTwo.Type.STATIC_RESOURCE);
        assertThat(vastResource.getCreativeType())
                .isEqualTo(VastResourceTwo.CreativeType.JAVASCRIPT);
    }

    @Test
    public void fromVastResourceXmlManager_withStaticType_withMissingCreativeType_shouldReturnNull() throws Exception {
        final VastResourceXmlManager resourceXmlManager =
                VastXmlManagerAggregatorTest.initializeVastResourceXmlManagerMock(
                        "StaticResource", null, "IFrameResource", "HTMLResource");

        assertThat(fromVastResourceXmlManager(
                resourceXmlManager, VastResourceTwo.Type.STATIC_RESOURCE, 50, 100)).isNull();
    }

    @Test
    public void fromVastResourceXmlManager_withStaticType_withInvalidCreativeType_shouldReturnNull() throws Exception {
        final VastResourceXmlManager resourceXmlManager =
                VastXmlManagerAggregatorTest.initializeVastResourceXmlManagerMock(
                        "StaticResource", "INVALID_CREATIVE_TYPE", "IFrameResource", "HTMLResource");

        assertThat(fromVastResourceXmlManager(
                resourceXmlManager, VastResourceTwo.Type.STATIC_RESOURCE, 50, 100)).isNull();
    }

    @Test
    public void fromVastResourceXmlManager_withNoResource_shouldReturnNull() throws Exception {
        final VastResourceXmlManager resourceXmlManager =
                VastXmlManagerAggregatorTest.initializeVastResourceXmlManagerMock(
                        null, null, null, null);

        assertThat(fromVastResourceXmlManager(
                resourceXmlManager, VastResourceTwo.Type.STATIC_RESOURCE, 50, 100)).isNull();
    }

    @Test
    public void constructor_shouldInitializeFieldsCorrectly() throws Exception {
        assertThat(subject.getResource()).isEqualTo("resource");
        assertThat(subject.getType()).isEqualTo(VastResourceTwo.Type.STATIC_RESOURCE);
        assertThat(subject.getCreativeType()).isEqualTo(VastResourceTwo.CreativeType.IMAGE);
    }

    @Test
    public void initializeWebView_withIFrameResource_shouldLoadData() throws Exception {
        subject = new VastResourceTwo("resource", VastResourceTwo.Type.IFRAME_RESOURCE, VastResourceTwo
                .CreativeType.NONE, 50, 100);
        subject.initializeWebView(mockVastWebView);

        verify(mockVastWebView).loadData(
                "<iframe frameborder=\"0\" scrolling=\"no\" marginheight=\"0\" " +
                        "marginwidth=\"0\" style=\"border: 0px; margin: 0px;\" width=\"50\" " +
                        "height=\"100\" src=\"resource\"></iframe>");
    }

    @Test
    public void initializeWebView_withHTMLResource_shouldLoadData() throws Exception {
        subject = new VastResourceTwo("resource", VastResourceTwo.Type.HTML_RESOURCE, VastResourceTwo
                .CreativeType.NONE, 50, 100);
        subject.initializeWebView(mockVastWebView);

        verify(mockVastWebView).loadData("resource");
    }

    @Test
    public void initializeWebView_withStaticResource_withImageCreativeType_shouldLoadData() throws Exception {
        subject = new VastResourceTwo("resource", VastResourceTwo.Type.STATIC_RESOURCE, VastResourceTwo
                .CreativeType.IMAGE, 50, 100);
        subject.initializeWebView(mockVastWebView);

        verify(mockVastWebView).loadData("<html><head></head><body style=\"margin:0;padding:0\">" +
                "<img src=\"resource\" width=\"100%\" style=\"max-width:100%;max-height:100%;\" />" +
                "</body></html>");
    }

    @Test
    public void initializeWebView_withStaticResource_withJavascriptCreativeType_shouldLoadData() throws Exception {
        subject = new VastResourceTwo("resource", VastResourceTwo.Type.STATIC_RESOURCE, VastResourceTwo
                .CreativeType.JAVASCRIPT, 50, 100);
        subject.initializeWebView(mockVastWebView);

        verify(mockVastWebView).loadData("<script src=\"resource\"></script>");
    }

    @Test
    public void getCorrectClickThroughUrl_shouldReturnCorrectClickThroughUrl() throws Exception {
        subject = new VastResourceTwo("resource", VastResourceTwo.Type.STATIC_RESOURCE, VastResourceTwo
                .CreativeType.IMAGE, 50, 100);
        assertThat(subject.getCorrectClickThroughUrl("xml", "web")).isEqualTo("xml");

        subject = new VastResourceTwo("resource", VastResourceTwo.Type.STATIC_RESOURCE, VastResourceTwo
                .CreativeType.JAVASCRIPT, 50, 100);
        assertThat(subject.getCorrectClickThroughUrl("xml", "web")).isEqualTo("web");

        subject = new VastResourceTwo("resource", VastResourceTwo.Type.HTML_RESOURCE, VastResourceTwo
                .CreativeType.NONE, 50, 100);
        assertThat(subject.getCorrectClickThroughUrl("xml", "web")).isEqualTo("web");

        subject = new VastResourceTwo("resource", VastResourceTwo.Type.IFRAME_RESOURCE, VastResourceTwo
                .CreativeType.NONE, 50, 100);
        assertThat(subject.getCorrectClickThroughUrl("xml", "web")).isEqualTo("web");

        subject = new VastResourceTwo("resource", VastResourceTwo.Type.HTML_RESOURCE, VastResourceTwo
                .CreativeType.NONE, 50, 100);
        assertThat(subject.getCorrectClickThroughUrl("xml", null)).isNull();

        subject = new VastResourceTwo("resource", VastResourceTwo.Type.IFRAME_RESOURCE, VastResourceTwo
                .CreativeType.NONE, 50, 100);
        assertThat(subject.getCorrectClickThroughUrl("xml", null)).isNull();

        subject = new VastResourceTwo("resource", VastResourceTwo.Type.STATIC_RESOURCE, VastResourceTwo
                .CreativeType.IMAGE, 50, 100);
        assertThat(subject.getCorrectClickThroughUrl("xml", null)).isEqualTo("xml");
    }
}

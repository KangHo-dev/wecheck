package org.afive.wecheck.like.controller;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.afive.wecheck.article.bean.ArticleBean;
import org.afive.wecheck.article.mapper.ArticleMapper;
import org.afive.wecheck.configuration.Data;
import org.afive.wecheck.configuration.ResponseCode;
import org.afive.wecheck.like.bean.ArticleLikeBean;
import org.afive.wecheck.like.bean.CommentLikeBean;
import org.afive.wecheck.like.mapper.ArticleLikeMapper;
import org.afive.wecheck.like.mapper.CommentLikeMapper;
import org.afive.wecheck.service.PushNotificationService;
import org.afive.wecheck.user.bean.AccessTokenBean;
import org.afive.wecheck.user.bean.FcmBean;
import org.afive.wecheck.user.bean.UserBean;
import org.afive.wecheck.user.mapper.AccessTokenMapper;
import org.afive.wecheck.user.mapper.FcmMapper;
import org.afive.wecheck.user.mapper.UserMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "like")
public class ArticleLikeController {
	@Autowired
	AccessTokenMapper accessTokenMapper;
	
	@Autowired
	ArticleMapper articleMapper;
	
	@Autowired
	ArticleLikeMapper articleLikeMapper;
	
	@Autowired
	UserMapper userMapper;
	
	@Autowired
	private FcmMapper fcmMapper;
	
	
	@RequestMapping(value = "/articles" ,method = RequestMethod.POST)
	private Map<String,Object> updateLike(
			@RequestHeader("Authorization") String accessTokenID ,
			@RequestParam(value = "articleID") String articleID ) {
		
		Map<String,Object> result = new HashMap<String,Object>();
		
		/*-- accessToken Data --*/
		AccessTokenBean accessTokenBean = accessTokenMapper.get(accessTokenID);
		if(accessTokenBean==null) {
			result.put("responseCode", String.valueOf(ResponseCode.ACCESS_DENIED_WRONG_ACCESSCODE));
			return result;
		}

		Integer userID = accessTokenBean.getUserID();
		
		
//		if(userID == null) {
//			System.out.println("userID ?????? ????????? articleID : "+articleID+" ??? Like??????");
//			result.put("responseCode", ResponseCode.ACCESS_DENIED_USERID_DOESNT_MATCH);
//			return result;
//		}
		
		/*
		 * article ????????? ?????? ??????
		 */
		ArticleBean articleBean = articleMapper.get(articleID);
		if(articleBean == null) {
			System.out.println("articleLike ??????????????? articleID : " + articleID + "??? article??? ??????");
			result.put("responseCode", ResponseCode.FAILED_NO_MATCH);
			return result;
		}
		
		int articleState = articleBean.getState();
		if(articleState != Data.ARTICLE_STATE_DEFAULT) {
			System.out.println("articleLike ??????????????? articleID : " + articleID + "??? article??? ??????(state??? deleted)???");
			result.put("responseCode", ResponseCode.ARTICLE_STATE_DELETED);
			return result;
		}
		
		ArticleLikeBean articleLikeBean = new ArticleLikeBean();
		articleLikeBean.setArticleID(Integer.parseInt(articleID));
		articleLikeBean.setUserID(userID);
		
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("userID",userID);
		map.put("articleID",articleID);
		
		Integer isChecked = articleLikeMapper.isCheckedByUserAndArticle(map);
		
		if(isChecked == null) {
			isChecked=0;
			articleLikeBean.setIsChecked(Data.ARTICLELIKE_CHECKED);
			articleLikeMapper.registerArticleLike(articleLikeBean);
		}
		else if(isChecked == 0) {
			articleLikeBean.setIsChecked(Data.ARTICLELIKE_CHECKED);
			articleLikeMapper.updateArticleLike(articleLikeBean);
		}
		else {
			articleLikeBean.setIsChecked(Data.ARTICLELIKE_DEFAULT);
			articleLikeMapper.updateArticleLike(articleLikeBean);
		}
		
		String likeCount = articleLikeMapper.getCountByArticleID(articleID);
		result.put("likeCount", likeCount);
		
		result.put("articleLike", articleLikeBean);
		
		/*
		 * ?????? ????????? ?????? ???????????? ????????????./////////////////////////////
		 * */
		
		if(isChecked==0 && articleBean.getUserID()!=userID) {
			UserBean userBean = userMapper.get(userID+"");
			FcmBean publisherFcmBean = fcmMapper.getByUserID(articleBean.getUserID()+"");

			if(publisherFcmBean == null) {
				System.out.println("publisher fcmBean ??? ???????????? ??????");
				result.put("responseCode", ResponseCode.SUCCESS);
				
				return result;
			}
			
			JSONObject jsonBody = new JSONObject();
			
			String pushType = Data.PUSH_TYPE_ARTICLE_LIKE+"";
			String idx=articleBean.getArticleID()+"";
			String title = "???????????? ???????????? ????";
			String body=userBean.getLastName()+" "+userBean.getFirstName()+"????????? [";
			
			if(articleBean.getTitle()!=null && articleBean.getContents().length()>0) {
				if(articleBean.getTitle().length()>20) {
					body+=articleBean.getTitle().substring(0, 20)+"...] ?????? ???????????? ???????????????";
				}else {
					body+=articleBean.getTitle()+"] ?????? ???????????? ???????????????";
				}
			}else {
				if(articleBean.getContents().length()>20) {
					body+=articleBean.getContents().substring(0, 20)+"...] ?????? ???????????? ???????????????";
				}else {
					body+=articleBean.getContents()+"] ?????? ???????????? ???????????????";
				}
			}
			
			jsonBody.put("to", publisherFcmBean.getFcmToken());
			
			if(publisherFcmBean.getDeviceType() == Data.DEVICE_TYPE_ANDROID) {
				JSONObject data = new JSONObject();
				data.put("title", title);
				data.put("body", body);
				data.put("pushType", pushType);
				data.put("idx", idx);
				
				jsonBody.put("data", data);
			}else if(publisherFcmBean.getDeviceType()==Data.DEVICE_TYPE_IOS) {
				JSONObject notification=new JSONObject();
				notification.put("title", title);
				notification.put("body", body);
				notification.put("pushType", pushType);
				notification.put("idx", idx);
				
				jsonBody.put("notification", notification);
				
			}
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(new MediaType("application","json",Charset.forName("UTF-8")));
			
			System.out.println("body : "+body.toString());
			HttpEntity<String> request = new HttpEntity<>(jsonBody.toString(), headers);
	
		
			CompletableFuture<String> pushNotification = PushNotificationService.send(request);
			CompletableFuture.allOf(pushNotification).join();
		}
		/////////?????? ????????? ??? ////////////////////////////////////
		
		result.put("responseCode", ResponseCode.SUCCESS);
		
		return result;
	}
}

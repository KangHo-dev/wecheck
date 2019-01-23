package org.afive.wecheck.article.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.afive.wecheck.article.bean.ArticleBean;
import org.afive.wecheck.article.bean.ArticleFileBean;
import org.afive.wecheck.article.bean.ArticleResult;
import org.afive.wecheck.article.bean.ArticleYouTubeBean;
import org.afive.wecheck.article.mapper.ArticleFileMapper;
import org.afive.wecheck.article.mapper.ArticleMapper;
import org.afive.wecheck.article.mapper.ArticleYouTubeMapper;
import org.afive.wecheck.comment.mapper.CommentMapper;
import org.afive.wecheck.configuration.BaseTool;
import org.afive.wecheck.configuration.Data;
import org.afive.wecheck.configuration.MemberType;
import org.afive.wecheck.configuration.ResponseCode;
import org.afive.wecheck.like.mapper.ArticleLikeMapper;
import org.afive.wecheck.user.bean.AccessTokenBean;
import org.afive.wecheck.user.bean.ConfirmRequestBean;
import org.afive.wecheck.user.bean.FcmBean;
import org.afive.wecheck.user.bean.SnsLoginBean;
import org.afive.wecheck.user.bean.UserBean;
import org.afive.wecheck.user.bean.UserResult;
import org.afive.wecheck.user.mapper.AccessTokenMapper;
import org.afive.wecheck.user.mapper.ConfirmRequestMapper;
import org.afive.wecheck.user.mapper.FcmMapper;
import org.afive.wecheck.user.mapper.SnsLoginMapper;
import org.afive.wecheck.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "articles")
public class ArticleController {
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private ArticleMapper articleMapper;
	
	@Autowired
	private AccessTokenMapper accessTokenMapper;
	
	@Autowired
	private ArticleLikeMapper articleLikeMapper;
	
	@Autowired
	private CommentMapper commentMapper;
	
	@Autowired
	private ArticleFileMapper articleFileMapper;
	
	@Autowired
	private ArticleYouTubeMapper articleYouTubeMapper;
	
//	//테스트용	
//	@RequestMapping(value ="",method = RequestMethod.GET) 
//	private Map<String, Object> getList(){
//		Map result = new HashMap();
//		
//		return result;
//	}
	
	@RequestMapping(value ="/{articleGroupID}/{pageNo}/{size}/{sort}/{privacy}/{regionID}/{unitID}",method = RequestMethod.GET) 
	private Map<String, Object> getList(
			@RequestHeader("Authorization") String accessTokenID,
			@PathVariable(value = "articleGroupID") String articleGroupIDstr,
			@PathVariable(value = "pageNo") String pageNoStr,
			@PathVariable(value = "size") String sizeStr,
			@PathVariable(value = "sort") String sortStr,
			@PathVariable(value = "privacy") String privacyStr,
			@PathVariable(value = "regionID") String regionID,
			@PathVariable(value = "unitID") String unitID
			){	
		Map<String, Object> result = new HashMap<String, Object>();
		
		AccessTokenBean accessTokenBean = accessTokenMapper.get(accessTokenID);
		
		if(accessTokenBean==null) {
			result.put("responseCode", String.valueOf(ResponseCode.ACCESS_DENIED_WRONG_ACCESSCODE));
			return result;
		}
		
//		System.out.println("articleGroupID : "+articleGroupIDstr+", pageNo : "+pageNoStr+", size : "+sizeStr+", sortStr : "+sortStr+", privacyStr : "+privacyStr+", regionID : "+regionID+", unitID : "+unitID);
		
		int pageNo=Integer.parseInt(pageNoStr);
		int size = Integer.parseInt(sizeStr);
		int privacy = Integer.parseInt(privacyStr);
		int articleGroupID=Integer.parseInt(articleGroupIDstr);
		
		
		pageNo=((pageNo-1)*size);
		
		List<ArticleBean> articleList;
		
//		System.out.println("넣는 값, privacy : "+privacy+", pageNo : "+pageNo+", size : "+size);
		
		
		//articleGroup으로 나눠져 있지 않
		if(articleGroupID==0) {
			
			articleList=articleMapper.getList(privacy, pageNo, size);
			
		//articleGroup으로 나눠져있음
		}else{
			articleList=articleMapper.getListFromArticleGroup(articleGroupID, privacy, pageNo, size);
		}
		
		
		/**
		 * 19-01-17 edited by gangho
		 */
		List<ArticleResult> articleResultList = new ArrayList<>();
		HashMap<String,Object> commentCountMap = new HashMap<>();
		HashMap<String,Object> checkMap = new HashMap<>();
		
		Integer userID = accessTokenBean.getUserID();
		
		for(int i = 0; i < articleList.size(); i++) {
			ArticleBean articleBean = articleList.get(i);
			ArticleResult articleResult = new ArticleResult(); 
			int articleID = articleBean.getArticleID();
			
			articleResult.setArticle(articleBean);
			
			UserBean publisherBean = userMapper.get(String.valueOf(userID));
			
			UserResult publisherResult = new UserResult();
			publisherResult.setUserID(publisherBean.getUserID());
			publisherResult.setUserType(publisherBean.getUserType());
			publisherResult.setState(publisherBean.getState());
			publisherResult.setLastName(publisherBean.getLastName()); 
			publisherResult.setFirstName(publisherBean.getFirstName());
			publisherResult.setProfileImage(publisherBean.getProfileImage());
			
			articleResult.setPublisher(publisherResult);
			
			//댓글 좋아요 수
			String articleLikeCount = articleLikeMapper.getCountByArticleID(String.valueOf(articleBean.getArticleID()));			
			articleResult.setLikeCount(articleLikeCount);
			
			//댓글 수
			String commentCount = commentMapper.getTotalCountByArticleID(String.valueOf(articleID));	
			articleResult.setCommentCount(commentCount);
			
			checkMap.put("userID",userID);
			checkMap.put("articleID",articleID);
			
			Integer isChecked = articleLikeMapper.isCheckedByUserAndArticle(checkMap);
			if(isChecked == null) {
				isChecked = 0;
			}
			
			articleResult.setLikeIsChecked(String.valueOf(isChecked));
			
			List<ArticleFileBean> articleFileList = articleFileMapper.getByArticleID(String.valueOf(articleID));
			List<ArticleYouTubeBean> youTubeList = articleYouTubeMapper.getListByArticleID(String.valueOf(articleID));
			
			//유튜브가 없는 경우
			if(youTubeList.isEmpty()) {
				
				// 유튜브는 없고 파일만 있는 경우
				if(!articleFileList.isEmpty()) {
					//이미지가 있는 경우
					if(articleFileList.get(0).getFileType()==0) {
						String rpImage = articleFileList.get(0).getArticleFilePath();
						articleResult.setRpImage(rpImage);
					}
				}
			}
			//유튜브가 있는경우- 대표이미지는 유튜브 섬네일
			else {
				articleResult.setThumbnail(true);
				/* 첫번째 영상의 id값 */
				String movieID = youTubeList.get(0).getMovieID();

				/*
				 * http://webdir.tistory.com/472 에 있는 내용을 근거로 한 섬네일
				 */
				articleResult.setRpImage(Data.YOUTUBE_THUMBNAIL_PATH + movieID + Data.YOUTUBE_THUMBNAIL_FORM);
				

			}
			
			articleResultList.add(articleResult);
		}
		
		result.put("articleList", articleResultList);
		
		result.put("responseCode", ResponseCode.SUCCESS);

		
		return result;
	}
	
}

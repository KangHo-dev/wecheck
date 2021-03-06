package org.afive.wecheck.configuration;

public class Data {
	
	public static final String FIREBASE_SERVER_KEY="AAAAHshO1GE:APA91bGUIo2vt-DYYpci2iYFCUnNUflzGdOcRrVrJOsbrbuTBsWZ6jLWLSxYne7LPZAGviayLjg7EEJsipvtjlrEbjxXnmjEabEQzSr8-6CtbaDTQs-hWrg5GI7_xK0O1oTe6ft1tfsn";
	public static final String FIREBASE_API_URL="https://fcm.googleapis.com/fcm/send";
	
	public static final int DEVICE_TYPE_ANDROID=0;
	public static final int DEVICE_TYPE_IOS = 1;
	public static final int DEVICE_TYPE_WEB = 2;
	
	public static final int PUSH_TYPE_CONFIRM_REQUEST_RESULT=0;
	public static final int PUSH_TYPE_ARTICLE_LIKE=1;
	public static final int PUSH_TYPE_COMMENT_LIKE=2;
	public static final int PUSH_TYPE_NEW_COMMENT=3;
	public static final int PUSH_TYPE_NEW_SUB_COMMENT=4;
	public final static int PUSH_TYPE_CHURCHSERVICE = 10; // 예배시간에 대한 PUSH메세지 타입
	
	public static final int PUSH_IDX_ACCEPTED=1; //0에서1로 수정함
	public static final int PUSH_IDX_DENIED=2; //1에서2로 수정함
	
	public static final int SERVICE_STATE_BEFORE=0;
	public static final int SERVICE_STATE_STARTED=1;
	public static final int SERVICE_STATE_FINISHED=2;
	public static final int SERVICE_STATE_CANCELED=3;
	
	/**
	 * created by gangho
	 */
	
//	public final static int COMMENT_PARENT_SIZE = 15; // 댓글 페이징시 불러오는 크기 //받는 파라미터로 설정하게끔 수정
//	public final static int COMMENT_CHILD_SIZE = 20; // 대댓글 페이징시 불러오는 크기 //받는 파라미터로 설정하게끔 수정
	public final static Integer COMMENT_PARENTID_DEFAULT = 0; // 추후에 null로 할꺼면 NULL로 변경
	
	public final static int ARTICLE_STATE_DEFAULT = 0;
	public final static int COMMENT_STATE_DEFAULT = 0;
	public final static int QUOTE_STATE_DEFAULT = 0;
	public final static int ARTICLE_STATE_DELETED = 1; // 유저가 지웠을때 state 1
	public final static int COMMENT_STATE_DELETED = 1; // 유저가 지웠을때 state 1
	
	public final static int COMMENTLIKE_DEFAULT = 0;
	public final static int COMMENTLIKE_CHECKED = 1;
	
	public final static int ARTICLELIKE_DEFAULT = 0;
	public final static int ARTICLELIKE_CHECKED = 1;
	
	/*
	 * http://webdir.tistory.com/472 에 있는 내용을 근거로 한 섬네일
	 */
	public final static String YOUTUBE_THUMBNAIL_PATH = "http://img.youtube.com/vi/";
	public final static String YOUTUBE_THUMBNAIL_FORM = "/hqdefault.jpg"; //고품질
	
	public final static int ATTENDANCE_DISTANCE_METER = 40;
}

package cn.cjgl.controller;

import cn.cjgl.config.TokenInfo;
import cn.cjgl.constant.Constant;
import cn.cjgl.service.FitbitDataService;
import cn.cjgl.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@Controller
@RequestMapping("FitbitData")
public class FitbitDataController {
	private static final Logger log = LoggerFactory.getLogger(FitbitDataController.class);
	
	@Autowired
    private FitbitDataService service;

	@Resource
	private S3Service s3Service;
	
	@RequestMapping("/queryList")
	@ResponseBody
	public Map<String, Object> queryList(@RequestParam("queryDate") String queryDate) {
		log.info("queryUserList>>>>>>");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("rows", new ArrayList<>());
		map.put("total", 0);

		/*if(!StringUtils.isNotEmpty(queryDate)){
			return map;
		}*/

		return map;
		//String queryDateNum=queryDate.replace("-","");
//		int endMinNum= DateTimeUtil.calculateMinutesFromMidnight(LocalDateTime.now());
//		int startMinNum=endMinNum>30?(endMinNum-30):0;
//		user.setStartminnum(startMinNum);
//		user.setEndminnum(endMinNum);
		/*FitbitData query=new FitbitData();
		if(!queryDate.equals("all")){
			query.setDaynum(Long.parseLong(queryDateNum));
		}
		List<FitbitData> userList = this.service.queryList(query);
		PageInfo<FitbitData> pageInfo = new PageInfo<FitbitData>(userList);

		map.put("rows", userList);
		map.put("total", pageInfo.getTotal());

		return map;*/
	}

	@RequestMapping("/getLatest")
	@ResponseBody
	public Object getLatest(@RequestParam("queryDate") String queryDate) {

		if(StringUtils.isEmpty(queryDate)){
			return null;
		}
		String queryDateNum=queryDate.replace("-","");
//		int endMinNum= DateTimeUtil.calculateMinutesFromMidnight(LocalDateTime.now());
//		int startMinNum=endMinNum>30?(endMinNum-30):0;
//		user.setStartminnum(startMinNum);
//		user.setEndminnum(endMinNum);
		//Cancel local query data
		/*FitbitData query=new FitbitData();
		if(!queryDate.equals("all")){
			query.setDaynum(Long.parseLong(queryDateNum));
		}
		List<FitbitData> list = this.service.queryList(query);
		if(CollectionUtils.isEmpty(list)){
			return null;
		}*/
		//Extract data from Amazon
		try {
			String key= TokenInfo.getUser_id()+"_"+ Constant.S3_DATA_TYPE_HEARTRATE+"_"+queryDateNum;
			return s3Service.getStringFromAws(Constant.S3_BUCKET_NAME,key);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return "";
	}
}

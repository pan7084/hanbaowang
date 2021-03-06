package com.upbest.mvc.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.upbest.mvc.entity.BExaminationPaper;
import com.upbest.pageModel.Json;
import com.upbest.utils.Constant;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.upbest.mvc.entity.BExamRefer;

import com.upbest.mvc.service.IExamSerivce;
import com.upbest.mvc.vo.ExamVO;
import com.upbest.utils.DataType;
import com.upbest.utils.PageModel;

@Controller
@RequestMapping(value = "/exam/")
public class ExamController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    IExamSerivce examService;
    @Autowired
    com.upbest.mvc.service.IExamService iExamSerivce;

    @RequestMapping(value = "/index")
    public String index(Model model) {
        model.addAttribute("current","exam");
        return "/bexam/examList";
    }
    @ResponseBody
    @RequestMapping("/listAllExamPaper")
    public void  listAllExamPaper(HttpServletResponse response){
        Gson gson=new Gson();
        List<BExaminationPaper>  paperList= iExamSerivce.getAllExamPaper();
        List<Map<String,Object>> mapResult=Lists.transform(paperList, new Function<BExaminationPaper, Map<String,Object>>() {
            @Override
            public Map<String, Object> apply(BExaminationPaper bExaminationPaper) {
                Map<String,Object> result=new HashMap ();
                result.put("id",bExaminationPaper.getId());
                result.put("name",bExaminationPaper.getEname());
                return result;
            }
        });
        outPrint(gson.toJson(mapResult),response);
    }
    @ResponseBody
    @RequestMapping("/list")
    public void list(@RequestParam(value = "examName", required = false) String name,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "rows", 
            required = false) Integer pageSize, 
            Model model,
            HttpSession session,
            HttpServletResponse response) {
        if (pageSize == null) {
            pageSize = 10;
        }
        PageRequest requestPage = new PageRequest(page != null ? page.intValue() - 1 : 0, pageSize, new Sort(Direction.DESC, new String[] { "id" }));
        Page<Object[]> work =examService.findExamList(name, requestPage);
        PageModel result=new PageModel();
        result.setPage(page);
        result.setRows(getWorkInfo(work.getContent()));
        result.setPageSize(pageSize);
        result.setRecords(NumberUtils.toInt(ObjectUtils.toString(work.getTotalElements())));
        String json = com.alibaba.fastjson.JSON.toJSONStringWithDateFormat(result, "yyyy-mm-dd HH:mm:ss", SerializerFeature.WriteMapNullValue);
        outPrint(json,response);
    }

 
    private List<ExamVO> getWorkInfo(List<Object[]> list){
        List<ExamVO> result=new ArrayList<ExamVO>();
        if(!CollectionUtils.isEmpty(list)){
            ExamVO entity=null;
            for(Object[] obj:list){
                entity=new ExamVO();
                entity.setId(DataType.getAsString(obj[0]));
                entity.setName(DataType.getAsString(obj[1]));
                entity.setTimer(DataType.getAsString(obj[2]));
                entity.setDate(DataType.getAsDate(obj[3]));
                entity.setNum(DataType.getAsString(obj[4]));
                entity.setType(DataType.getAsString(obj[5]));
                result.add(entity);
            }
        }
        return result;
    }
    
    private void outPrint(String json, HttpServletResponse response) {
        try {
            PrintWriter writer = response.getWriter();
            writer.write(json);
            writer.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    @RequestMapping(value = "/create")
    public String createForm(String id, Model model) {
        if(StringUtils.isNotBlank(id)){
            BExamRefer exam = examService.findById(Integer.parseInt(id));
            model.addAttribute("exam",exam);
        }
        return "/bexam/addExam";
    }
    @ResponseBody
    @RequestMapping("/add")
    public void list(@RequestParam(value = "jsons", required = false) String vo,
            Model model, HttpSession session, HttpServletResponse response) {
        BExamRefer exam= new BExamRefer();
        JSONObject jso = JSONObject.parseObject(vo);
        exam = (BExamRefer) JSONObject.toJavaObject(jso,
                BExamRefer.class);   
        examService.saveBExamRefer(exam);
    }
    @ResponseBody
    @RequestMapping("/del")
    public void del(@RequestParam(value = "id", required = false) String id, Model model, HttpServletResponse response, HttpServletRequest request) {
        examService.deleteById(Integer.parseInt(id));
        outPrint("0", response);
    }
    
    @RequestMapping(value = "get")
    public String get(String id, Model model, HttpSession session) {
        BExamRefer exam = examService.findById(Integer.parseInt(id));
        model.addAttribute("exam",exam);
        return "/bexam/getExam";
    }
    @RequestMapping(value = "/findRepeatLostScoreItem")
    @ResponseBody
    public Json securi_findRepeatLostScoreItem(Integer shopId,HttpServletRequest req) throws Exception {
        Json result = new Json();


        if (shopId == null) {
            result.setCode(com.upbest.mvc.constant.Constant.Code.NULL_CODE);
            result.setMsg("门店编号为空");
            result.setObj(null);
            result.setSuccess(false);
            return result;
        }
        try {
            result.setObj(iExamSerivce.findRepeatLostScoreItem(shopId));
            result.setCode(com.upbest.mvc.constant.Constant.Code.SUCCESS_CODE);
            result.setMsg("获取成功");
            result.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(com.upbest.mvc.constant.Constant.Code.ILLEGAL_CODE);
            result.setMsg("获取失败");
            result.setSuccess(false);
        }
        return result;
    }
    @RequestMapping(value = "/findLastExamScore")
    @ResponseBody
    public Json securi_findLastExamScore(Integer shopId,Integer examType, HttpServletRequest req) throws Exception {
        Json result = new Json();


        if (shopId == null || examType == null) {
            result.setCode(com.upbest.mvc.constant.Constant.Code.NULL_CODE);
            result.setMsg("shopId,examType为空");
            result.setObj(null);
            result.setSuccess(false);
            return result;
        }
        try {
            result.setObj(iExamSerivce.findLastExamScore(shopId, examType));
            result.setCode(com.upbest.mvc.constant.Constant.Code.SUCCESS_CODE);
            result.setMsg("获取成功");
            result.setSuccess(true);
        } catch (Exception e) {
            result.setCode(com.upbest.mvc.constant.Constant.Code.ILLEGAL_CODE);
            result.setMsg("获取失败");
            result.setSuccess(false);
        }
        return result;
    }
}

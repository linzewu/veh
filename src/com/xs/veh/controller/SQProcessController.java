package com.xs.veh.controller;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.manager.SQProcessManager;
import com.xs.veh.manager.VehManager;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/sqp"})
public class SQProcessController {

   @Value("${jyjgbh}")
   private String jyjgbh;
   @Resource(
      name = "vehManager"
   )
   private VehManager vehManager;
   @Resource(
      name = "sqProcessManager"
   )
   private SQProcessManager sqProcessManager;


   @RequestMapping(
      value = {"save"},
      method = {RequestMethod.POST}
   )
   @ResponseBody
   public String vehLogin(@RequestParam("jylsh") String jylsh) {
      VehCheckLogin vehInfo = this.vehManager.getVehCheckLoginByJylsh(this.jyjgbh, jylsh);
      List vcps = this.vehManager.getVehCheckPrcoessByJylsh(jylsh);
      VehCheckProcess sqvpc = null;
      Iterator var6 = vcps.iterator();

      while(var6.hasNext()) {
         VehCheckProcess cc = (VehCheckProcess)var6.next();
         if(cc.getJyxm().equals("sq")) {
            sqvpc = cc;
            break;
         }
      }

      if(sqvpc == null) {
         sqvpc = new VehCheckProcess();
         sqvpc.setClsbdh(vehInfo.getClsbdh());
         sqvpc.setJylsh(jylsh);
         sqvpc.setHphm(vehInfo.getHphm());
         sqvpc.setHpzl(vehInfo.getHpzl());
         sqvpc.setJyxm("sq");
      }
      
     

      Calendar cc1 = Calendar.getInstance();
      sqvpc.setKssj(cc1.getTime());
      cc1.add(13, 30);
      sqvpc.setJssj(cc1.getTime());
      sqvpc.setJycs(vehInfo.getJycs());
      sqvpc.setStatus(0);
      sqvpc.setVoideSate(0);
      this.sqProcessManager.save(sqvpc);
      return "OK";
   }
}

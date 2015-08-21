/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.Componet.Vector;

import java.util.ArrayList;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public interface LMCropVector {

    public LMCropVector getVector();

    public void setAll(ArrayList<String> a);

    public Boolean CeckIfCorrect();

    public ArrayList<String> getAll();

    public String getRowForSave();

    public LMCropVector clone();
    
    public Boolean isEmpty();

    //Getter And Setter Methods
    //Getter And Setter ------>ID
    public void setID(int i);
    public int getID();
    //Getter And Setter ------>IDC
    public void setIdc(int i);
    public int getIdc();
    //Getter And Setter ------>CPNM
    public void setCpnm(String s);
    public String getCpnm();
    //Getter And Setter ------>CROPNAME
    public void setCropname(String s);
    public String getCropname();
    //Getter And Setter ------>PHU
    public void setPhu(Double d);
    public Double getPhu();
    //Getter And Setter ------>RUE
    public void setRue(Double d);
    public Double getRue();
    //Getter And Setter ------>HVSTI
    public void setHvsti(Double d);
    public Double getHvsti();
    //Getter And Setter ------>MLAI
    public void setMlai(Double d);
    public Double getMlai();
    //Getter And Setter ------>FRGRW1
    public void setFrgrw1 (Double d);
    public Double getFrgrw1();
    //Getter And Setter ------>FRGRW2
    public void setFrgrw2 (Double d);
    public Double getFrgrw2();
    //Getter And Setter ------>LAIMX1
    public void setLaimx1 (Double d);
    public Double getLaimx1 ();
    //Getter And Setter ------>LAIMX2
    public void setLaimx2 (Double d);
    public Double getLaimx2 ();
    //Getter And Setter ------>DLAI
    public void setDlai (Double d);
    public Double getDlai();
    //Getter And Setter ------>CHTMX
    public void setChtmx(Double d);
    public Double getChtmx();
    //Getter And Setter ------>RDMX
    public void setRdmx(Double d);
    public Double getRdmx();
    //Getter And Setter ------>TOPT
    public void setTopt(Double d);
    public Double getTopt();
    //Getter And Setter ------>TBASE
    public void setTbase(Double d);
    public Double getTbase();
    //Getter And Setter ------>CNYLD
    public void setCnyld(Double d);
    public Double getCnyld();
    //Getter And Setter ------>CPYLD
    public void setCpyld(Double d);
    public Double getCpyld();
    //Getter And Setter ------>BN1
    public void setBn1(Double d);
    public Double getBn1();
    //Getter And Setter ------>BN2
    public void setBn2(Double d);
    public Double getBn2();
    //Getter And Setter ------>BN3
    public void setBn3(Double d);
    public Double getBn3();
    //Getter And Setter ------>BP1
    public void setBp1(Double d);
    public Double getBp1();
    //Getter And Setter ------>BP2
    public void setBp2(Double d);
    public Double getBp2();
    //Getter And Setter ------>BP3
    public void setBp3(Double d);
    public Double getBp3();
    //Getter And Setter ------>WYYF
    public void setWsyf(Double d);
    public Double getWsyf();
    //Getter And Setter ------>GSI
    public void setGsi(Double d);
    public Double getGsi();
    //Getter And Setter ------>VPDFR
    public void setVpdfr(Double d);
    public Double getVpdfr();
    //Getter And Setter ------>FRGMAX
    public void setFrgmax(Double d);
    public Double getFrgmax();
    //Getter And Setter ------>WAVP
    public void setWavp(Double d);
    public Double getWavp();
    //Getter And Setter ------>CO2HI
    public void setCo2hi(Double d);
    public Double getCo2hi();
    //Getter And Setter ------>BIOEHI
    public void setBioehi(Double d);
    public Double getBioehi();
    //Getter And Setter ------>RSDCO_PL
    public void setRsdco_pl(Double d);
    public Double getRsdco_pl();
    //Getter And Setter ------>LID
    public void setLid(Double d);
    public Double getLid();
    //Getter And Setter ------>HU_EC1
    public void setHu_ec1(Double d);
    public Double getHu_ec1();
    //Getter And Setter ------>HU_EC2
    public void setHu_ec2(Double d);
    public Double getHu_ec2();
    //Getter And Setter ------>HU_EC3
    public void setHu_ec3(Double d);
    public Double getHu_ec3();
    //Getter And Setter ------>HU_EC4
    public void setHu_ec4(Double d);
    public Double getHu_ec4();
    //Getter And Setter ------>HU_EC5
    public void setHu_ec5(Double d);
    public Double getHu_ec5();
    //Getter And Setter ------>HU_EC6
    public void setHu_ec6(Double d);
    public Double getHu_ec6();
    //Getter And Setter ------>HU_EC7
    public void setHu_ec7(Double d);
    public Double getHu_ec7();
    //Getter And Setter ------>HU_EC8
    public void setHu_ec8(Double d);
    public Double getHu_ec8();
    //Getter And Setter ------>HU_EC9
    public void setHu_ec9(Double d);
    public Double getHu_ec9();
    //Getter And Setter ------>LAIMIN
    public void setLai_min(Double d);
    public Double getLai_min();
    //Getter And Setter ------>endbioN
    public void setEndbion(Double d);
    public Double getEndbion();
    //Getter And Setter ------>maxfert
    public void setMaxfert(Double d);
    public Double getMaxfert();
    //Getter And Setter ------>bion02
    public void setBion02(Double d);
    public Double getBion02();
    //Getter And Setter ------>bion04
    public void setBion04(Double d);
    public Double getBion04();
    //Getter And Setter ------>bion06
    public void setBion06(Double d);
    public Double getBion06();
    //Getter And Setter ------>bion08
    public void setBion08(Double d);
    public Double getBion08();


}

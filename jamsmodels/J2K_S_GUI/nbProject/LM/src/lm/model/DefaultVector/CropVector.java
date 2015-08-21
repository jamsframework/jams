package lm.model.DefaultVector;

import java.util.ArrayList;
import lm.Componet.Vector.LMCropVector;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CropVector implements LMCropVector {

    private int ID;
    private int idc;
    private String cpnm;
    private String cropname;
    private Double phu;
    private Double rue;
    private Double hvsti;
    private Double mlai;
    private Double frgrw1;
    private Double frgrw2;
    private Double laimx1;
    private Double laimx2;
    private Double dlai;
    private Double chtmx;
    private Double rdmx;
    private Double topt;
    private Double tbase;
    private Double cnyld;
    private Double cpyld;
    private Double bn1;
    private Double bn2;
    private Double bn3;
    private Double bp1;
    private Double bp2;
    private Double bp3;
    private Double wsyf;
    private Double gsi;
    private Double vpdfr;
    private Double frgmax;
    private Double wavp;
    private Double co2hi;
    private Double bioehi;
    private Double rsdco_pl;
    private Double lid;
    private Double hu_ec1;
    private Double hu_ec2;
    private Double hu_ec3;
    private Double hu_ec4;
    private Double hu_ec5;
    private Double hu_ec6;
    private Double hu_ec7;
    private Double hu_ec8;
    private Double hu_ec9;
    private Double lai_min;
    private Double endbion;
    private Double maxfert;
    private Double bion02;
    private Double bion04;
    private Double bion06;
    private Double bion08;

    public CropVector(){

        this.ID=0;
        this.idc=0;
        this.cpnm=null;
        this.cropname=null;
        this.phu=null;
        this.rue=null;
        this.hvsti=null;
        this.mlai=null;
        this.frgrw1=null;
        this.frgrw2=null;
        this.laimx1=null;
        this.laimx2=null;
        this.dlai=null;
        this.chtmx=null;
        this.rdmx=null;
        this.topt=null;
        this.tbase=null;
        this.cnyld=null;
        this.cpyld=null;
        this.bn1=null;
        this.bn2=null;
        this.bn3=null;
        this.bp1=null;
        this.bp2=null;
        this.bp3=null;
        this.wsyf=null;
        this.gsi=null;
        this.vpdfr=null;
        this.frgmax=null;
        this.wavp=null;
        this.co2hi=null;
        this.bioehi=null;
        this.rsdco_pl=null;
        this.lid=null;
        this.hu_ec1=null;
        this.hu_ec2=null;
        this.hu_ec3=null;
        this.hu_ec4=null;
        this.hu_ec5=null;
        this.hu_ec6=null;
        this.hu_ec7=null;
        this.hu_ec8=null;
        this.hu_ec9=null;
        this.lai_min=null;
        this.endbion=null;
        this.maxfert=null;
        this.bion02=null;
        this.bion04=null;
        this.bion06=null;
        this.bion08=null;
    }

    public CropVector(ArrayList<String> a){
        this.ID=Integer.parseInt(a.get(0));
        this.idc=Integer.parseInt(a.get(1));
        this.cpnm=a.get(2);
        this.cropname=a.get(3);
        this.phu=Double.valueOf(a.get(4));
        this.rue=Double.valueOf(a.get(5));
        this.hvsti=Double.valueOf(a.get(6));
        this.mlai=Double.valueOf(a.get(7));
        this.frgrw1=Double.valueOf(a.get(8));
        this.frgrw2=Double.valueOf(a.get(9));
        this.laimx1=Double.valueOf(a.get(10));
        this.laimx2=Double.valueOf(a.get(11));
        this.dlai=Double.valueOf(a.get(12));
        this.chtmx=Double.valueOf(a.get(13));
        this.rdmx=Double.valueOf(a.get(14));
        this.topt=Double.valueOf(a.get(15));
        this.tbase=Double.valueOf(a.get(16));
        this.cnyld=Double.valueOf(a.get(17));
        this.cpyld=Double.valueOf(a.get(18));
        this.bn1=Double.valueOf(a.get(19));
        this.bn2=Double.valueOf(a.get(20));
        this.bn3=Double.valueOf(a.get(21));
        this.bp1=Double.valueOf(a.get(22));
        this.bp2=Double.valueOf(a.get(23));
        this.bp3=Double.valueOf(a.get(24));
        this.wsyf=Double.valueOf(a.get(25));
        this.gsi=Double.valueOf(a.get(26));
        this.vpdfr=Double.valueOf(a.get(27));
        this.frgmax=Double.valueOf(a.get(28));
        this.wavp=Double.valueOf(a.get(29));
        this.co2hi=Double.valueOf(a.get(30));
        this.bioehi=Double.valueOf(a.get(31));
        this.rsdco_pl=Double.valueOf(a.get(32));
        this.lid=Double.valueOf(a.get(33));
        this.hu_ec1=Double.valueOf(a.get(34));
        this.hu_ec2=Double.valueOf(a.get(35));
        this.hu_ec3=Double.valueOf(a.get(36));
        this.hu_ec4=Double.valueOf(a.get(37));
        this.hu_ec5=Double.valueOf(a.get(38));
        this.hu_ec6=Double.valueOf(a.get(39));
        this.hu_ec7=Double.valueOf(a.get(40));
        this.hu_ec8=Double.valueOf(a.get(41));
        this.hu_ec9=Double.valueOf(a.get(42));
        this.lai_min=Double.valueOf(a.get(43));
        this.endbion=Double.valueOf(a.get(44));
        this.maxfert=Double.valueOf(a.get(45));
        this.bion02=Double.valueOf(a.get(46));
        this.bion04=Double.valueOf(a.get(47));
        this.bion06=Double.valueOf(a.get(48));
        this.bion08=Double.valueOf(a.get(49));
    }

    public void setAll(ArrayList<String> a){
        this.ID=Integer.parseInt(a.get(0));
        this.idc=Integer.parseInt(a.get(1));
        this.cpnm=a.get(2);
        this.cropname=a.get(3);
        this.phu=Double.valueOf(a.get(4));
        this.rue=Double.valueOf(a.get(5));
        this.hvsti=Double.valueOf(a.get(6));
        this.mlai=Double.valueOf(a.get(7));
        this.frgrw1=Double.valueOf(a.get(8));
        this.frgrw2=Double.valueOf(a.get(9));
        this.laimx1=Double.valueOf(a.get(10));
        this.laimx2=Double.valueOf(a.get(11));
        this.dlai=Double.valueOf(a.get(12));
        this.chtmx=Double.valueOf(a.get(13));
        this.rdmx=Double.valueOf(a.get(14));
        this.topt=Double.valueOf(a.get(15));
        this.tbase=Double.valueOf(a.get(16));
        this.cnyld=Double.valueOf(a.get(17));
        this.cpyld=Double.valueOf(a.get(18));
        this.bn1=Double.valueOf(a.get(19));
        this.bn2=Double.valueOf(a.get(20));
        this.bn3=Double.valueOf(a.get(21));
        this.bp1=Double.valueOf(a.get(22));
        this.bp2=Double.valueOf(a.get(23));
        this.bp3=Double.valueOf(a.get(24));
        this.wsyf=Double.valueOf(a.get(25));
        this.gsi=Double.valueOf(a.get(26));
        this.vpdfr=Double.valueOf(a.get(27));
        this.frgmax=Double.valueOf(a.get(28));
        this.wavp=Double.valueOf(a.get(29));
        this.co2hi=Double.valueOf(a.get(30));
        this.bioehi=Double.valueOf(a.get(31));
        this.rsdco_pl=Double.valueOf(a.get(32));
        this.lid=Double.valueOf(a.get(33));
        this.hu_ec1=Double.valueOf(a.get(34));
        this.hu_ec2=Double.valueOf(a.get(35));
        this.hu_ec3=Double.valueOf(a.get(36));
        this.hu_ec4=Double.valueOf(a.get(37));
        this.hu_ec5=Double.valueOf(a.get(38));
        this.hu_ec6=Double.valueOf(a.get(39));
        this.hu_ec7=Double.valueOf(a.get(40));
        this.hu_ec8=Double.valueOf(a.get(41));
        this.hu_ec9=Double.valueOf(a.get(42));
        this.lai_min=Double.valueOf(a.get(43));
        this.endbion=Double.valueOf(a.get(44));
        this.maxfert=Double.valueOf(a.get(45));
        this.bion02=Double.valueOf(a.get(46));
        this.bion04=Double.valueOf(a.get(47));
        this.bion06=Double.valueOf(a.get(48));
        this.bion08=Double.valueOf(a.get(49));
    }

    public LMCropVector getVector() {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }
    

    public Boolean CeckIfCorrect() {
        throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("ressources/Ressources").getString("NOT SUPPORTED YET."));
    }

    public String getRowForSave(){
        ArrayList<String>a=this.getAll();
        String s="";
        s=s+a.get(0);
        for(int i=1;i<a.size();i++){
           s=s+"\t"+a.get(i);
        }
        return s;
    }

    public LMCropVector clone(){
        return new CropVector(this.getAll());
    }

    public ArrayList<String> getAll(){
        ArrayList<String> a=new ArrayList();
        a.add(ID+"");
        a.add(idc+"");
        a.add(cpnm+"");
        a.add(cropname+"");
        a.add(phu+"");
        a.add(rue+"");
        a.add(hvsti+"");
        a.add(mlai+"");
        a.add(frgrw1+"");
        a.add(frgrw2+"");
        a.add(laimx1+"");
        a.add(laimx2+"");
        a.add(dlai+"");
        a.add(chtmx+"");
        a.add(rdmx+"");
        a.add(topt+"");
        a.add(tbase+"");
        a.add(cnyld+"");
        a.add(cpyld+"");
        a.add(bn1+"");
        a.add(bn2+"");
        a.add(bn3+"");
        a.add(bp1+"");
        a.add(bp2+"");
        a.add(bp3+"");
        a.add(wsyf+"");
        a.add(gsi+"");
        a.add(vpdfr+"");
        a.add(frgmax+"");
        a.add(wavp+"");
        a.add(co2hi+"");
        a.add(bioehi+"");
        a.add(rsdco_pl+"");
        a.add(lid+"");
        a.add(hu_ec1+"");
        a.add(hu_ec2+"");
        a.add(hu_ec3+"");
        a.add(hu_ec4+"");
        a.add(hu_ec5+"");
        a.add(hu_ec6+"");
        a.add(hu_ec7+"");
        a.add(hu_ec8+"");
        a.add(hu_ec9+"");
        a.add(lai_min+"");
        a.add(endbion+"");
        a.add(maxfert+"");
        a.add(bion02+"");
        a.add(bion04+"");
        a.add(bion06+"");
        a.add(bion08+"");
        return a;
    }

    public Boolean isEmpty() {
        if(getID()==0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * @return the ID
     */
    public int getID() {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * @return the idc
     */
    public int getIdc() {
        return idc;
    }

    /**
     * @param idc the idc to set
     */
    public void setIdc(int idc) {
        this.idc = idc;
    }

    /**
     * @return the cpnm
     */
    public String getCpnm() {
        return cpnm;
    }

    /**
     * @param cpnm the cpnm to set
     */
    public void setCpnm(String cpnm) {
        this.cpnm = cpnm;
    }

    /**
     * @return the cropname
     */
    public String getCropname() {
        return cropname;
    }

    /**
     * @param cropname the cropname to set
     */
    public void setCropname(String cropname) {
        this.cropname = cropname;
    }

    /**
     * @return the phu
     */
    public Double getPhu() {
        return phu;
    }

    /**
     * @param phu the phu to set
     */
    public void setPhu(Double phu) {
        this.phu = phu;
    }

    /**
     * @return the rue
     */
    public Double getRue() {
        return rue;
    }

    /**
     * @param rue the rue to set
     */
    public void setRue(Double rue) {
        this.rue = rue;
    }

    /**
     * @return the hvsti
     */
    public Double getHvsti() {
        return hvsti;
    }

    /**
     * @param hvsti the hvsti to set
     */
    public void setHvsti(Double hvsti) {
        this.hvsti = hvsti;
    }

    /**
     * @return the mlai
     */
    public Double getMlai() {
        return mlai;
    }

    /**
     * @param mlai the mlai to set
     */
    public void setMlai(Double mlai) {
        this.mlai = mlai;
    }

    /**
     * @return the frgrw1
     */
    public Double getFrgrw1() {
        return frgrw1;
    }

    /**
     * @param frgrw1 the frgrw1 to set
     */
    public void setFrgrw1(Double frgrw1) {
        this.frgrw1 = frgrw1;
    }

    /**
     * @return the frgrw2
     */
    public Double getFrgrw2() {
        return frgrw2;
    }

    /**
     * @param frgrw2 the frgrw2 to set
     */
    public void setFrgrw2(Double frgrw2) {
        this.frgrw2 = frgrw2;
    }

    /**
     * @return the laimx1
     */
    public Double getLaimx1() {
        return laimx1;
    }

    /**
     * @param laimx1 the laimx1 to set
     */
    public void setLaimx1(Double laimx1) {
        this.laimx1 = laimx1;
    }

    /**
     * @return the laimx2
     */
    public Double getLaimx2() {
        return laimx2;
    }

    /**
     * @param laimx2 the laimx2 to set
     */
    public void setLaimx2(Double laimx2) {
        this.laimx2 = laimx2;
    }

    /**
     * @return the dlai
     */
    public Double getDlai() {
        return dlai;
    }

    /**
     * @param dlai the dlai to set
     */
    public void setDlai(Double dlai) {
        this.dlai = dlai;
    }

    /**
     * @return the chtmx
     */
    public Double getChtmx() {
        return chtmx;
    }

    /**
     * @param chtmx the chtmx to set
     */
    public void setChtmx(Double chtmx) {
        this.chtmx = chtmx;
    }

    /**
     * @return the rdmx
     */
    public Double getRdmx() {
        return rdmx;
    }

    /**
     * @param rdmx the rdmx to set
     */
    public void setRdmx(Double rdmx) {
        this.rdmx = rdmx;
    }

    /**
     * @return the topt
     */
    public Double getTopt() {
        return topt;
    }

    /**
     * @param topt the topt to set
     */
    public void setTopt(Double topt) {
        this.topt = topt;
    }

    /**
     * @return the tbase
     */
    public Double getTbase() {
        return tbase;
    }

    /**
     * @param tbase the tbase to set
     */
    public void setTbase(Double tbase) {
        this.tbase = tbase;
    }

    /**
     * @return the cnyld
     */
    public Double getCnyld() {
        return cnyld;
    }

    /**
     * @param cnyld the cnyld to set
     */
    public void setCnyld(Double cnyld) {
        this.cnyld = cnyld;
    }

    /**
     * @return the cpyld
     */
    public Double getCpyld() {
        return cpyld;
    }

    /**
     * @param cpyld the cpyld to set
     */
    public void setCpyld(Double cpyld) {
        this.cpyld = cpyld;
    }

    /**
     * @return the bn1
     */
    public Double getBn1() {
        return bn1;
    }

    /**
     * @param bn1 the bn1 to set
     */
    public void setBn1(Double bn1) {
        this.bn1 = bn1;
    }

    /**
     * @return the bn2
     */
    public Double getBn2() {
        return bn2;
    }

    /**
     * @param bn2 the bn2 to set
     */
    public void setBn2(Double bn2) {
        this.bn2 = bn2;
    }

    /**
     * @return the bn3
     */
    public Double getBn3() {
        return bn3;
    }

    /**
     * @param bn3 the bn3 to set
     */
    public void setBn3(Double bn3) {
        this.bn3 = bn3;
    }

    /**
     * @return the bp1
     */
    public Double getBp1() {
        return bp1;
    }

    /**
     * @param bp1 the bp1 to set
     */
    public void setBp1(Double bp1) {
        this.bp1 = bp1;
    }

    /**
     * @return the bp2
     */
    public Double getBp2() {
        return bp2;
    }

    /**
     * @param bp2 the bp2 to set
     */
    public void setBp2(Double bp2) {
        this.bp2 = bp2;
    }

    /**
     * @return the bp3
     */
    public Double getBp3() {
        return bp3;
    }

    /**
     * @param bp3 the bp3 to set
     */
    public void setBp3(Double bp3) {
        this.bp3 = bp3;
    }

    /**
     * @return the wsyf
     */
    public Double getWsyf() {
        return wsyf;
    }

    /**
     * @param wsyf the wsyf to set
     */
    public void setWsyf(Double wsyf) {
        this.wsyf = wsyf;
    }

    /**
     * @return the gsi
     */
    public Double getGsi() {
        return gsi;
    }

    /**
     * @param gsi the gsi to set
     */
    public void setGsi(Double gsi) {
        this.gsi = gsi;
    }

    /**
     * @return the vpdfr
     */
    public Double getVpdfr() {
        return vpdfr;
    }

    /**
     * @param vpdfr the vpdfr to set
     */
    public void setVpdfr(Double vpdfr) {
        this.vpdfr = vpdfr;
    }

    /**
     * @return the frgmax
     */
    public Double getFrgmax() {
        return frgmax;
    }

    /**
     * @param frgmax the frgmax to set
     */
    public void setFrgmax(Double frgmax) {
        this.frgmax = frgmax;
    }

    /**
     * @return the wavp
     */
    public Double getWavp() {
        return wavp;
    }

    /**
     * @param wavp the wavp to set
     */
    public void setWavp(Double wavp) {
        this.wavp = wavp;
    }

    /**
     * @return the co2hi
     */
    public Double getCo2hi() {
        return co2hi;
    }

    /**
     * @param co2hi the co2hi to set
     */
    public void setCo2hi(Double co2hi) {
        this.co2hi = co2hi;
    }

    /**
     * @return the bioehi
     */
    public Double getBioehi() {
        return bioehi;
    }

    /**
     * @param bioehi the bioehi to set
     */
    public void setBioehi(Double bioehi) {
        this.bioehi = bioehi;
    }

    /**
     * @return the rsdco_pl
     */
    public Double getRsdco_pl() {
        return rsdco_pl;
    }

    /**
     * @param rsdco_pl the rsdco_pl to set
     */
    public void setRsdco_pl(Double rsdco_pl) {
        this.rsdco_pl = rsdco_pl;
    }

    /**
     * @return the lid
     */
    public Double getLid() {
        return lid;
    }

    /**
     * @param lid the lid to set
     */
    public void setLid(Double lid) {
        this.lid = lid;
    }

    /**
     * @return the hu_ec1
     */
    public Double getHu_ec1() {
        return hu_ec1;
    }

    /**
     * @param hu_ec1 the hu_ec1 to set
     */
    public void setHu_ec1(Double hu_ec1) {
        this.hu_ec1 = hu_ec1;
    }

    /**
     * @return the hu_ec2
     */
    public Double getHu_ec2() {
        return hu_ec2;
    }

    /**
     * @param hu_ec2 the hu_ec2 to set
     */
    public void setHu_ec2(Double hu_ec2) {
        this.hu_ec2 = hu_ec2;
    }

    /**
     * @return the hu_ec3
     */
    public Double getHu_ec3() {
        return hu_ec3;
    }

    /**
     * @param hu_ec3 the hu_ec3 to set
     */
    public void setHu_ec3(Double hu_ec3) {
        this.hu_ec3 = hu_ec3;
    }

    /**
     * @return the hu_ec4
     */
    public Double getHu_ec4() {
        return hu_ec4;
    }

    /**
     * @param hu_ec4 the hu_ec4 to set
     */
    public void setHu_ec4(Double hu_ec4) {
        this.hu_ec4 = hu_ec4;
    }

    /**
     * @return the hu_ec5
     */
    public Double getHu_ec5() {
        return hu_ec5;
    }

    /**
     * @param hu_ec5 the hu_ec5 to set
     */
    public void setHu_ec5(Double hu_ec5) {
        this.hu_ec5 = hu_ec5;
    }

    /**
     * @return the hu_ec6
     */
    public Double getHu_ec6() {
        return hu_ec6;
    }

    /**
     * @param hu_ec6 the hu_ec6 to set
     */
    public void setHu_ec6(Double hu_ec6) {
        this.hu_ec6 = hu_ec6;
    }

    /**
     * @return the hu_ec7
     */
    public Double getHu_ec7() {
        return hu_ec7;
    }

    /**
     * @param hu_ec7 the hu_ec7 to set
     */
    public void setHu_ec7(Double hu_ec7) {
        this.hu_ec7 = hu_ec7;
    }

    /**
     * @return the hu_ec8
     */
    public Double getHu_ec8() {
        return hu_ec8;
    }

    /**
     * @param hu_ec8 the hu_ec8 to set
     */
    public void setHu_ec8(Double hu_ec8) {
        this.hu_ec8 = hu_ec8;
    }

    /**
     * @return the hu_ec9
     */
    public Double getHu_ec9() {
        return hu_ec9;
    }

    /**
     * @param hu_ec9 the hu_ec9 to set
     */
    public void setHu_ec9(Double hu_ec9) {
        this.hu_ec9 = hu_ec9;
    }

    /**
     * @return the lai_min
     */
    public Double getLai_min() {
        return lai_min;
    }

    /**
     * @param lai_min the lai_min to set
     */
    public void setLai_min(Double lai_min) {
        this.lai_min = lai_min;
    }

    /**
     * @return the endbioN
     */
    public Double getEndbion() {
        return endbion;
    }

    /**
     * @param endbioN the endbioN to set
     */
    public void setEndbion(Double endbion) {
        this.endbion = endbion;
    }

    /**
     * @return the maxfert
     */
    public Double getMaxfert() {
        return maxfert;
    }

    /**
     * @param maxfert the maxfert to set
     */
    public void setMaxfert(Double maxfert) {
        this.maxfert = maxfert;
    }

    /**
     * @return the bion02
     */
    public Double getBion02() {
        return bion02;
    }

    /**
     * @param bion02 the bion02 to set
     */
    public void setBion02(Double bion02) {
        this.bion02 = bion02;
    }

    /**
     * @return the bion04
     */
    public Double getBion04() {
        return bion04;
    }

    /**
     * @param bion04 the bion04 to set
     */
    public void setBion04(Double bion04) {
        this.bion04 = bion04;
    }

    /**
     * @return the bion06
     */
    public Double getBion06() {
        return bion06;
    }

    /**
     * @param bion06 the bion06 to set
     */
    public void setBion06(Double bion06) {
        this.bion06 = bion06;
    }

    /**
     * @return the bion08
     */
    public Double getBion08() {
        return bion08;
    }

    /**
     * @param bion08 the bion08 to set
     */
    public void setBion08(Double bion08) {
        this.bion08 = bion08;
    }

}

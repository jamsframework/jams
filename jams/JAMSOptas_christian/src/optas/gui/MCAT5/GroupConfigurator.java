/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui.MCAT5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import optas.hydro.ParameterGroup;
import optas.data.SimpleEnsemble;

/**
 *
 * @author chris
 */
public class GroupConfigurator {
    double beta = 0.8;
    double Tmin = 0.2;

    JPanel groupConfigurationPanel;

    JPanel configuration = new JPanel();

    JLabel betaLabel = new JLabel("beta");
    JLabel minDominanceLabel = new JLabel("T_min");

    JTextField betaTextField = new JTextField(Double.toString(beta),5);
    JTextField tMinField = new JTextField(Double.toString(Tmin),5);

    JLabel infoLabel = new JLabel("info");

    JPanel groups = new JPanel();
    JList groupList = new JList();

    JButton addGroup = new JButton("+");
    JButton delGroup = new JButton("-");

    JPanel parameterInGroup = new JPanel();
    JList availableList = new JList();
    JList inGroupList = new JList();
    JButton addParameter = new JButton(">>");
    JButton rmParameter = new JButton("<<");

    JPanel infoPanel = new JPanel();
    JButton updateBn = new JButton("Update");

    ArrayList<ParameterGroup> solutionGroup = new ArrayList<ParameterGroup>();

    Set<ActionListener> listeners = new HashSet<ActionListener>();

    SimpleEnsemble[] p;
    int n;

    boolean isUpdated = false;

    public GroupConfigurator(){
        this.p = new SimpleEnsemble[0];
        this.n = 0;

        layout();
        init();
    }

    public void setData(SimpleEnsemble p[], int n){
        this.p = p;
        this.n = n;
        
        update();
    }

    public void setSolutionGroup(ArrayList<ParameterGroup> solutionGroup){
        this.solutionGroup = solutionGroup;
        update();
    }

    private void layout(){

        configuration.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Configuration"));
        groups.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Groups"));
        parameterInGroup.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Parameter"));

        JScrollPane inGroupListScroll = new JScrollPane(inGroupList);
        inGroupList.setFixedCellWidth(170);
        groupList.setFixedCellWidth(100);
        JScrollPane availableListScroll = new JScrollPane(this.availableList);
        JScrollPane groupListScroll = new JScrollPane(this.groupList);
        
        GroupLayout tmpLayout = new GroupLayout(configuration);
        configuration.setLayout(tmpLayout);

        tmpLayout.setHorizontalGroup(
                tmpLayout.createSequentialGroup()
                .addComponent(betaLabel)
                .addComponent(betaTextField)
                .addComponent(minDominanceLabel)
                .addComponent(tMinField)
                );

        tmpLayout.setVerticalGroup(
                tmpLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(betaLabel)
                .addComponent(betaTextField)
                .addComponent(minDominanceLabel)
                .addComponent(tMinField)
                );
        
        tmpLayout = new GroupLayout(groups);
        groups.setLayout(tmpLayout);

        tmpLayout.setHorizontalGroup(
                tmpLayout.createSequentialGroup()
                .addComponent(groupListScroll)
                .addGroup(
                tmpLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(addGroup)
                .addComponent(delGroup)
                ));

        tmpLayout.setVerticalGroup(
                tmpLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(groupListScroll)
                .addGroup(
                tmpLayout.createSequentialGroup()
                .addComponent(addGroup)
                .addComponent(delGroup)
                ));
        
        tmpLayout = new GroupLayout(parameterInGroup);
        parameterInGroup.setLayout(tmpLayout);

        tmpLayout.setHorizontalGroup(
                tmpLayout.createSequentialGroup()
                .addComponent(availableListScroll)
                .addGroup(
                    tmpLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(addParameter)
                        .addComponent(rmParameter)
                )
                .addComponent(inGroupListScroll)
        );

        tmpLayout.setVerticalGroup(
                tmpLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(availableListScroll)
                .addGroup(
                    tmpLayout.createSequentialGroup()
                        .addComponent(addParameter)
                        .addComponent(rmParameter)
                )
                .addComponent(inGroupListScroll)
        );

        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(infoLabel, BorderLayout.WEST);

        groupConfigurationPanel = new JPanel();
        GroupLayout layout = new GroupLayout(groupConfigurationPanel);
        groupConfigurationPanel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(configuration)
            .addGroup(
                layout.createSequentialGroup()
                .addComponent(groups)
                .addComponent(parameterInGroup)
            )
            .addComponent(infoPanel)
            .addComponent(updateBn)
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addComponent(configuration)
            .addGroup(
                layout.createParallelGroup()
                .addComponent(groups)
                .addComponent(parameterInGroup)
            )
            .addComponent(infoPanel)
            .addComponent(updateBn)
        );
    }

    public int getSelectedGroup(){
        return this.groupList.getSelectedIndex();
    }
    public JPanel getPanel(){
        return groupConfigurationPanel;
    }

    private void init(){
        addGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ParameterGroup newGroup = new ParameterGroup(p,n);
                newGroup = newGroup.createEmptyGroup();
                solutionGroup.add(newGroup);
                update();
            }
        });

        delGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = groupList.getSelectedIndex();
                if (index != -1){
                    solutionGroup.remove(index);
                }
                update();
            }
        });

        addParameter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int groupIndices[] = groupList.getSelectedIndices();
                for (int groupIndex : groupIndices) {
                    if (groupIndex == -1) {
                        return;
                    }

                    ParameterGroup group = solutionGroup.get(groupIndex);

                    Object list[] = availableList.getSelectedValues(); //new in jdk1.7 .. so keep it for a moment
                    for (Object o : list) {
                        if (o != null && o instanceof SimpleEnsemble) {
                            SimpleEnsemble pAdd = (SimpleEnsemble) o;
                            for (int i = 0; i < n; i++) {
                                if (p[i] == pAdd) {
                                    group.add(i);
                                }
                            }
                        }
                    }
                }
                update();
            }
        });

        rmParameter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int groupIndices[] = groupList.getSelectedIndices();
                for (int groupIndex : groupIndices) {
                    if (groupIndex == -1) {
                        return;
                    }

                    ParameterGroup group = solutionGroup.get(groupIndex);

                    int indices[] = inGroupList.getSelectedIndices();
                    for (int index : indices)
                        group.remove(index);

                    update();
                }
            }
        });

        groupList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                
                int index = groupList.getSelectedIndex();
                if (index == -1)
                    return;
                ParameterGroup group = solutionGroup.get(index);
                DefaultListModel inGroupModel = new DefaultListModel();
                for (int i=0;i<group.getSize();i++){
                    inGroupModel.addElement(p[group.get(i)]);
                }
                inGroupList.setModel(inGroupModel);
                fireSelectionChange();
            }
        });

        updateBn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fireUpdate();
            }
        });
    }

    private void update(){
        DefaultListModel groupListModel = new DefaultListModel();
        int counter = 1;
        boolean inUse[] = new boolean[n];
        for (ParameterGroup p : solutionGroup){
            groupListModel.addElement("Group" + counter++);

            for (int i=0;i<p.getSize();i++){
                inUse[p.get(i)] = true;
            }
        }
        Object o = groupList.getSelectedValue();
        this.groupList.setModel(groupListModel);
        groupList.setSelectedValue(o, true);

        DefaultListModel availableListModel = new DefaultListModel();

        for (int i=0;i<n;i++){
            //if (!inUse[i]){
                availableListModel.addElement(this.p[i]);
            //}
        }
        this.availableList.setModel(availableListModel);

        this.inGroupList.setModel(new DefaultListModel());
        isUpdated = false;

        int index = groupList.getSelectedIndex();
        if (index == -1) {
            return;
        }
        ParameterGroup group = solutionGroup.get(index);
        DefaultListModel inGroupModel = new DefaultListModel();
        for (int i = 0; i < group.getSize(); i++) {
            inGroupModel.addElement(p[group.get(i)]);
        }
        inGroupList.setModel(inGroupModel);
    }

    public void setDominanceInfo(double value){
        this.infoLabel.setText("Dominance:" + 100.*value + "%");
    }

    public ArrayList<ParameterGroup> getGroupConfiguration(){
        return this.solutionGroup;
    }
    public double getBeta(){
        return this.beta;
    }
    public double getTemporalDominanceThreshold(){
        return this.Tmin;
    }

    public void setBeta(double beta){
        this.beta = beta;
    }
    public void getTemporalDominanceThreshold(double Tmin){
        this.Tmin = Tmin;
    }

    public void addUpdateListener(ActionListener listener){
        listeners.add(listener);
    }

    private void fireUpdate(){
        ActionEvent e = new ActionEvent(this, 0, "update");
        for (ActionListener listener : listeners){
            listener.actionPerformed(e);
        }
        isUpdated = true;
    }
    
    private void fireSelectionChange(){
        if (!isUpdated)
            return;
        
        ActionEvent e = new ActionEvent(this, 0, "select");
        for (ActionListener listener : listeners){
            listener.actionPerformed(e);
        }
    }
}

package filters;

import section.Section;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import model.RoadModel;

import org.jblas.DoubleMatrix;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import doubleMatrix.Concat;
import doubleMatrix.InverseMatrix;
import trueSolution.TrueSolution;
import trueSolutionCTM.TrueSolutionCTM;

public class Estimation {

	RoadModel roadModel;	
	Filter filter;
	boolean consensus;


	public Estimation(RoadModel _roadModel, String _nameFilter, boolean _consensus) {
		roadModel = _roadModel;	
		filter = Filter.createFilter(this);			
		consensus=_consensus;
	}
	
	public void nextStepWithoutUpdateTrue() {
		filter.nextStep();
	}
	public void nextStepNoDataWithoutUpdateTrue() {
		filter.nextStepNoData();
	}
	
	public void updateTrueSolution() {
		roadModel.updateTrueSolution();//
	}

	public DoubleMatrix propagate(DoubleMatrix _density) {		
      return roadModel.propagate(_density);
	}
	
	public DoubleMatrix getDensityMean() {
		return roadModel.getDensityMean(filter.mean);
	}
	
	public void setSection(Section _section){
		roadModel.setSetion(_section);
		roadModel.updateModelVar();
	}

	public static void exportResult(TrueSolutionCTM trueSolutionCTM, TrueSolution[] trueSolution,TrueSolution[] trueSolution1, TrueSolution[] trueSolution2,int limite, String folder) {
		try {		
			
			int bdry=1;//*set boundary type
			int numSections = trueSolution[0].numSections;
			int numSections1 = trueSolution1[0].numSections;
			int numSections2 = trueSolution2[0].numSections;
			
			BufferedWriter[] writerSection = new BufferedWriter[numSections];
			BufferedWriter[] writerSection1 = new BufferedWriter[numSections1];
			BufferedWriter[] writerSection2 = new BufferedWriter[numSections2];
			
			BufferedWriter[] LyapSection = new BufferedWriter[numSections];
			
			BufferedWriter writerTrue;
			BufferedWriter writerConsensusTerm;
			
			BufferedWriter writerAvr;
			BufferedWriter writerAvr1;
			BufferedWriter writerAvr2;

			BufferedWriter Error;
			BufferedWriter Error1;
			BufferedWriter Error2;
			
			BufferedWriter Lyap;

			BufferedWriter[] Mode= new BufferedWriter[numSections];
			BufferedWriter[] Mode1= new BufferedWriter[numSections1];
			BufferedWriter[] Mode2= new BufferedWriter[numSections2];
			BufferedWriter[] GammaWrite= new BufferedWriter[numSections];

			BufferedWriter writerDisagreement;
			BufferedWriter writerDisagreement1;
			BufferedWriter writerDisagreement2;

			System.out.print("Beginning of simulation ");
				
				new File("results/"+folder).mkdir();
				
				RoadModel[] roadModels=new RoadModel[numSections];
				RoadModel[] roadModels1=new RoadModel[numSections1];
				RoadModel[] roadModels2=new RoadModel[numSections2];
				
				for (int i=0;i<numSections;i++){					
					trueSolution[i].section=trueSolution[i].setSections();					
				}				
				for (int i=0;i<numSections1;i++){					
					trueSolution1[i].section=trueSolution1[i].setSections();					
				}				
				for (int i=0;i<numSections2;i++){					
					trueSolution2[i].section=trueSolution2[i].setSections();					
				}
				
				for (int i=0;i<numSections;i++){					
					roadModels[i]=trueSolution[i].setRoadModels();
				}				
				for (int i=0;i<numSections1;i++){					
					roadModels1[i]=trueSolution1[i].setRoadModels();
				}				
				for (int i=0;i<numSections2;i++){					
					roadModels2[i]=trueSolution2[i].setRoadModels();
				}
				
				writerTrue = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"trueState.csv")));
				writerConsensusTerm = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"consensusTerm.csv")));
				writerAvr = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"writerAvr.csv")));
				writerAvr1 = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"writerAvr1.csv")));
				writerAvr2 = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"writerAvr2.csv")));				
				Error = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"Error.csv")));
				Error1 = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"Error1.csv")));
				Error2 = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"Error2.csv")));
				Lyap = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"Lyapunov.csv")));
				writerDisagreement = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"Disagreement.csv")));
				writerDisagreement1 = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"Disagreement1.csv")));
				writerDisagreement2 = new BufferedWriter(new FileWriter(new File("results/"+folder+"/"+"Disagreement2.csv")));
								
				
				
				
				Estimation[] estimations = new Estimation[numSections];
				Estimation[] estimations1 = new Estimation[numSections1];
				Estimation[] estimations2 = new Estimation[numSections2];

				for (int i = 0; i<numSections; i++) {
					estimations[i] = new Estimation(roadModels[i], "KCF",true);//*set the last variable to indicate if consensus is needed				
					String S = "results/"+folder+"/"+roadModels[i].nameModel;
					writerSection[i] = new BufferedWriter(new FileWriter(new File(S+".csv")));
					Mode[i]=new BufferedWriter(new FileWriter(new File(S+"Mode.csv")));
					GammaWrite[i]=new BufferedWriter(new FileWriter(new File(S+"Gamma.csv")));
					LyapSection[i]=new BufferedWriter(new FileWriter(new File(S+"LyapSection.csv")));
				} 
				
				for (int i = 0; i<numSections1; i++) {					
					estimations1[i] = new Estimation(roadModels1[i], "KCF",false);//*set the last variable to indicate if consensus is needed
					String S = "results/"+folder+"/"+roadModels1[i].nameModel;				
					writerSection1[i] = new BufferedWriter(new FileWriter(new File(S+"_1.csv")));					
					Mode1[i]=new BufferedWriter(new FileWriter(new File(S+"Mode1.csv")));
				}
				
				for (int i = 0; i<numSections2; i++) {
					estimations2[i] = new Estimation(roadModels2[i], "KCF",false);//*set the last variable to indicate if consensus is needed
					String S = "results/"+folder+"/"+roadModels2[i].nameModel;					
					writerSection2[i] = new BufferedWriter(new FileWriter(new File(S+"_2.csv")));					
					Mode2[i]=new BufferedWriter(new FileWriter(new File(S+"Mode2.csv")));								
				}

				int overlap=roadModels[0].trueSolution.overlapsec;
				int cellsec=roadModels[0].section.cellsec;
				int overlap1=roadModels1[0].trueSolution.overlapsec;
				int cellsec1=roadModels1[0].section.cellsec;
				int overlap2=roadModels2[0].trueSolution.overlapsec;
				int cellsec2=roadModels2[0].section.cellsec;
				
				
				
				
				
				DoubleMatrix I1=DoubleMatrix.zeros(roadModels[0].section.cellsec,overlap);
				for (int i=0;i<overlap;i++){
					I1.put(i,i,1);
				}
				DoubleMatrix I2=DoubleMatrix.zeros(roadModels[0].section.cellsec,overlap);
				for (int i=roadModels[0].section.cellsec-overlap;i<roadModels[0].section.cellsec;i++){
					I2.put(i,i-(roadModels[0].section.cellsec-overlap),1);
				}
				DoubleMatrix I123=DoubleMatrix.concatVertically(I1.transpose(), I2.transpose());
				DoubleMatrix H_hat=I2.transpose();
				for (int i=0;i<numSections-2;i++){
					H_hat=Concat.Diagonal(H_hat, I123);
				}
				H_hat=Concat.Diagonal(H_hat, I1.transpose());
				DoubleMatrix L_tilde=DoubleMatrix.zeros((numSections-1)*2*overlap, numSections*cellsec);				
				for(int i=0;i<numSections-1;i++){
					for(int j=0;j<overlap;j++){
						L_tilde.put(i*(2*overlap)+j,(i+1)*(cellsec)+j, 1);
					}
					for(int j=0;j<overlap;j++){
						L_tilde.put(i*(2*overlap)+overlap+j,(i+1)*(cellsec)-overlap+j, 1);
					}
					for(int j=0;j<overlap;j++){
						L_tilde.put(i*(2*overlap)+j,(i+1)*(cellsec)-overlap+j, -1);
					}
					for(int j=0;j<overlap;j++){
						L_tilde.put(i*(2*overlap)+overlap+j,(i+1)*(cellsec)+j, -1);
					}
				}				
				DoubleMatrix[] S=new DoubleMatrix[numSections];			
				trueSolutionCTM.getMeasurement();
				for (int i = 0; i<numSections; i++) {
					if (i==0){
						estimations[i].filter.Ltilde=L_tilde.getRange(i, i+overlap, i, i+2*cellsec);
			
					}
					else if (i==numSections-1){
						estimations[i].filter.Ltilde=L_tilde.getRange((2*(numSections-1)-1)*overlap, (2*(numSections-1)-1)*overlap+overlap, (i-1)*cellsec, (i+1)*cellsec);
					
					}
					else{
						estimations[i].filter.Ltilde=L_tilde.getRange((2*i-1)*overlap, (2*i+1)*overlap, (i-1)*cellsec, (i+2)*cellsec);
						
					}
					estimations[i].filter.I1=I1;
					estimations[i].filter.I2=I2;
					

				}
				
				double[]disagAvg=new double [3];
				double[]errorAvg=new double [3];
				double[]errAvgEntire=new double [3];
				
				

				for (int k=0; k<limite; k++) {
					System.out.println("time step="+k);		
					for (int i=0;i<trueSolutionCTM.trueStatesCTM.getRows();i++){
						writerTrue.write(trueSolutionCTM.trueStatesCTM.get(i,0)+",");
					}
					writerTrue.write("\n");				
					DoubleMatrix[] mean = new DoubleMatrix[numSections];
					DoubleMatrix[] mean_1 = new DoubleMatrix[numSections1];
					DoubleMatrix[] mean_2 = new DoubleMatrix[numSections2];					
					double ErrorAvg=0;
					double ErrorEntire=0;
					double ErrorAvg1=0;
					double ErrorAvg2=0;
					double LyapAvg=0;
					
					double[]errorAvgk=new double [3];
					for (int i=0; i<numSections; i++) {
						mean [i] = estimations[i].getDensityMean();
						if (numSections==1){
							for (int j=0; j<mean[i].length ; j++) {
								writerAvr.write(mean[i].get(j)+",");
								errorAvgk[0]=(mean[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))*(mean[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))+errorAvgk[0];
							}
						}
						else{
							if (i==0){
								for (int j=0; j<mean[i].length-overlap ; j++) {
									writerAvr.write(mean[i].get(j)+",");
									errorAvgk[0]=(mean[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))*(mean[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))+errorAvgk[0];
								}
							}
							else if (i>0 && i<numSections-1){
								for (int j=0; j<overlap ; j++) {
									writerAvr.write((mean[i].get(j)+mean[i-1].get(cellsec-overlap+j))/2+",");
									errorAvgk[0]=((mean[i].get(j)+mean[i-1].get(cellsec-overlap+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean[i].length-overlap)+j,0))*((mean[i].get(j)+mean[i-1].get(cellsec-overlap+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean[i].length-overlap)+j,0))+errorAvgk[0];
								}
								for (int j=0; j<mean[i].length-2*overlap ; j++) {
									writerAvr.write(mean[i].get(overlap+j)+",");
									errorAvgk[0]=(mean[i].get(overlap+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean[i].length-overlap)+overlap+j,0))*(mean[i].get(overlap+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean[i].length-overlap)+overlap+j,0))+errorAvgk[0];
								}
							}
							else if (i==numSections-1){
								for (int j=0; j<overlap ; j++) {
									writerAvr.write((mean[i].get(j)+mean[i-1].get(cellsec-overlap+j))/2+",");
									errorAvgk[0]=((mean[i].get(j)+mean[i-1].get(cellsec-overlap+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean[i].length-overlap)+j,0))*((mean[i].get(j)+mean[i-1].get(cellsec-overlap+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean[i].length-overlap)+j,0))+errorAvgk[0];
								}
								for (int j=0; j<mean[i].length-overlap ; j++) {
									writerAvr.write(mean[i].get(overlap+j)+",");	
									errorAvgk[0]=(mean[i].get(overlap+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean[i].length-overlap)+overlap+j,0))*(mean[i].get(overlap+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean[i].length-overlap)+overlap+j,0))+errorAvgk[0];
								}
							}
						}
						
						for (int j=0; j<mean[i].length ; j++) {
							writerSection[i].write(mean[i].get(j)+",");							
						}					
						double errorSection=(((mean[i].sub(trueSolution[i].trueStates)).transpose().mmul((mean[i].sub(trueSolution[i].trueStates)))).get(0,0))/((double)cellsec);
						Error.write(errorSection+",");				
						ErrorAvg=ErrorAvg+errorSection/((double)numSections);
						writerSection[i].write("\n");
						double lyapunovSection=((mean[i].sub(trueSolution[i].trueStates)).transpose()).mmul(InverseMatrix.invPoSym(estimations[i].filter.var)).mmul(mean[i].sub(trueSolution[i].trueStates)).get(0,0);
						double tracevar=0;
						for (int c=0;c<mean[i].getRows();c++){
							tracevar=tracevar+estimations[i].filter.var.get(c,c);
						}
						
						
						LyapSection[i].write(lyapunovSection+",");
						LyapSection[i].write(tracevar+"\n");
						if (i!=0){
							LyapAvg=LyapAvg+lyapunovSection/((double)(numSections-1));
						}
						
						
						
					}	
					errAvgEntire[0]=errorAvgk[0]/((double)(limite*trueSolutionCTM.cells))+errAvgEntire[0];
					Error.write(ErrorAvg+",");
					errorAvg[0]=errorAvg[0]+ErrorAvg/((double)(limite));
					Lyap.write(LyapAvg+"\n");
					
					for (int i=0; i<numSections1; i++) {
						mean_1 [i] = estimations1[i].getDensityMean();
						if (numSections1==1){
							for (int j=0; j<mean_1[i].length ; j++) {
								writerAvr1.write(mean_1[i].get(j)+",");	
								errorAvgk[1]=(mean_1[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))*(mean_1[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))+errorAvgk[1];
							}
						}
						else{
							if (i==0){
								for (int j=0; j<mean_1[i].length-overlap1 ; j++) {
									writerAvr1.write(mean_1[i].get(j)+",");		
									errorAvgk[1]=(mean_1[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))*(mean_1[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))+errorAvgk[1];
								}
							}
							else if (i>0 && i<numSections1-1){
								for (int j=0; j<overlap1 ; j++) {
									writerAvr1.write((mean_1[i].get(j)+mean_1[i-1].get(cellsec1-overlap1+j))/2+",");	
									errorAvgk[1]=((mean_1[i].get(j)+mean_1[i-1].get(cellsec1-overlap1+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean_1[i].length-overlap1)+j,0))*((mean_1[i].get(j)+mean_1[i-1].get(cellsec1-overlap1+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean_1[i].length-overlap1)+j,0))+errorAvgk[1];
								}
								for (int j=0; j<mean_1[i].length-2*overlap1 ; j++) {
									writerAvr1.write(mean_1[i].get(overlap1+j)+",");	
									errorAvgk[1]=(mean_1[i].get(overlap1+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean_1[i].length-overlap1)+overlap1+j,0))*(mean_1[i].get(overlap1+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean_1[i].length-overlap1)+overlap1+j,0))+errorAvgk[1];
								}
							}
							else if (i==numSections1-1){
								for (int j=0; j<overlap1 ; j++) {
									writerAvr1.write((mean_1[i].get(j)+mean_1[i-1].get(cellsec1-overlap1+j))/2+",");	
									errorAvgk[1]=((mean_1[i].get(j)+mean_1[i-1].get(cellsec1-overlap1+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean_1[i].length-overlap1)+j,0))*((mean_1[i].get(j)+mean_1[i-1].get(cellsec1-overlap1+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean_1[i].length-overlap1)+j,0))+errorAvgk[1];
								}
								for (int j=0; j<mean_1[i].length-overlap1 ; j++) {
									writerAvr1.write(mean_1[i].get(overlap1+j)+",");	
									errorAvgk[1]=(mean_1[i].get(overlap1+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean_1[i].length-overlap1)+overlap1+j,0))*(mean_1[i].get(overlap1+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean_1[i].length-overlap1)+overlap1+j,0))+errorAvgk[1];
								}
							}
						}
						for (int j=0; j<mean_1[i].length ; j++) {
							writerSection1[i].write(mean_1[i].get(j)+",");						
						}						
						double errorSection1=(((mean_1[i].sub(trueSolution1[i].trueStates).transpose()).mmul((mean_1[i].sub(trueSolution1[i].trueStates)))).get(0,0))/((double)cellsec1);
						Error1.write(errorSection1+",");					
						ErrorAvg1=ErrorAvg1+errorSection1/((double)numSections1);
						writerSection1[i].write("\n");					
					}
					Error1.write(ErrorAvg1+",");
					errorAvg[1]=errorAvg[1]+ErrorAvg1/((double)(limite));
					errAvgEntire[1]=errorAvgk[1]/((double)(limite*trueSolutionCTM.cells))+errAvgEntire[1];
					
					for (int i=0; i<numSections2; i++) {					
						mean_2 [i] = estimations2[i].getDensityMean();
						if (numSections2==1){
							for (int j=0; j<mean_2[i].length ; j++) {
								writerAvr2.write(mean_2[i].get(j)+",");		
								errorAvgk[2]=(mean_2[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))*(mean_2[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))+errorAvgk[2];
							}
						}
						else{
							if (i==0){
								for (int j=0; j<mean_2[i].length-overlap2 ; j++) {
									writerAvr2.write(mean_2[i].get(j)+",");	
									errorAvgk[2]=(mean_2[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))*(mean_2[i].get(j)-trueSolutionCTM.trueStatesCTM.get(j,0))+errorAvgk[2];
								}
							}
							else if (i>0 && i<numSections2-1){
								for (int j=0; j<overlap2 ; j++) {
									writerAvr2.write((mean_2[i].get(j)+mean_2[i-1].get(cellsec2-overlap2+j))/2+",");
									errorAvgk[2]=((mean_2[i].get(j)+mean_2[i-1].get(cellsec2-overlap2+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean_2[i].length-overlap2)+j,0))*((mean_2[i].get(j)+mean_2[i-1].get(cellsec2-overlap2+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean_2[i].length-overlap2)+j,0))+errorAvgk[2];
								}
								for (int j=0; j<mean_2[i].length-2*overlap2 ; j++) {
									writerAvr2.write(mean_2[i].get(overlap2+j)+",");	
									errorAvgk[2]=(mean_2[i].get(overlap2+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean_2[i].length-overlap2)+overlap2+j,0))*(mean_2[i].get(overlap2+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean_2[i].length-overlap2)+overlap2+j,0))+errorAvgk[2];
								}
							}
							else if (i==numSections2-1){
								for (int j=0; j<overlap2 ; j++) {
									writerAvr2.write((mean_2[i].get(j)+mean_2[i-1].get(cellsec2-overlap2+j))/2+",");	
									errorAvgk[2]=((mean_2[i].get(j)+mean_2[i-1].get(cellsec2-overlap2+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean_2[i].length-overlap2)+j,0))*((mean_2[i].get(j)+mean_2[i-1].get(cellsec2-overlap2+j))/2-trueSolutionCTM.trueStatesCTM.get(i*(mean_2[i].length-overlap2)+j,0))+errorAvgk[2];
								}
								for (int j=0; j<mean_2[i].length-overlap2 ; j++) {
									writerAvr2.write(mean_2[i].get(overlap2+j)+",");	
									errorAvgk[2]=(mean_2[i].get(overlap2+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean_2[i].length-overlap2)+overlap2+j,0))*(mean_2[i].get(overlap2+j)-trueSolutionCTM.trueStatesCTM.get(i*(mean_2[i].length-overlap2)+overlap2+j,0))+errorAvgk[2];
								
									
								}
							}
						}
						for (int j=0; j<mean_2[i].length ; j++) {
							writerSection2[i].write(mean_2[i].get(j)+",");						
						}
						double errorSection2=(((mean_2[i].sub(trueSolution2[i].trueStates)).transpose().mmul((mean_2[i].sub(trueSolution2[i].trueStates)))).get(0,0))/((double)(cellsec2));
						Error2.write(errorSection2+",");						
						ErrorAvg2=ErrorAvg2+errorSection2/((double)numSections2);
						writerSection2[i].write("\n");					
					}
					Error2.write(ErrorAvg2+",");
					errorAvg[2]=errorAvg[2]+ErrorAvg2/((double)(limite));
					errAvgEntire[2]=errorAvgk[2]/((double)(limite*trueSolutionCTM.cells))+errAvgEntire[2];
				
					writerAvr.write("\n");
					writerAvr1.write("\n");
					writerAvr2.write("\n");
					
					Error.write("\n");
					Error1.write("\n");
					Error2.write("\n");					
					
					Section[] secs=new Section[numSections];
					Section[] secs1=new Section[numSections1];
					Section[] secs2=new Section[numSections2];
					
					for(int i=0;i<numSections;i++){
						secs[i]=trueSolution[i].setSections();//every step, update mode
					}
					for(int i=0;i<numSections1;i++){
						secs1[i]=trueSolution1[i].setSections();
					}
					for(int i=0;i<numSections2;i++){
						secs2[i]=trueSolution2[i].setSections();
					}
					for(int i=0;i<numSections;i++){
						secs[i].Estimates=mean[i];
					}
					for(int i=0;i<numSections1;i++){
						secs1[i].Estimates=mean_1[i];
					}
					for(int i=0;i<numSections2;i++){
						secs2[i].Estimates=mean_2[i];
					}

					for(int i=0;i<numSections;i++){
						if(secs[i].getClass().getSimpleName().equals("FC")){
							secs[i].getwavefrontfromEstimation();
							if(secs[i].wavefront<0){
								secs[i].wavefront=0;
							}
							if(secs[i].wavefront<=cellsec-2){
							     if(trueSolution[i].flux(secs[i].Estimates.get(secs[i].wavefront,0))-trueSolution[i].flux(secs[i].Estimates.get(secs[i].wavefront+1,0))>0){							
								     secs[i].wavedirection=0;							
						         }
						         else{
									 secs[i].wavedirection=1;							
						         }
							}
							else if (secs[i].wavefront==cellsec-1){
								if(secs[i].index!=numSections-1){
									if(trueSolution[i].flux(secs[i].Estimates.get(secs[i].wavefront,0))-trueSolution[i+1].flux(secs[i].Estimates.get(overlap,0))>0){										
										secs[i].wavedirection=0;									
								    }
								    else{					
										secs[i].wavedirection=1;									
								    }
								}
								else{
									secs[i].wavedirection=0;
								}							
							}
							secs[i].getModelA();
				 		    secs[i].getModelB1();
						    secs[i].getModelB2();						   						
						}
						else if (secs[i].getClass().getSimpleName().equals("CF")){
							secs[i].getwavefrontfromEstimation();
							secs[i].getModelA();
				 		    secs[i].getModelB1();
						    secs[i].getModelB2();
						}						
					}
					
					for(int i=0;i<numSections1;i++){
						if(secs1[i].getClass().getSimpleName().equals("FC")){
							secs1[i].getwavefrontfromEstimation();
							if(secs1[i].wavefront<0){
								secs1[i].wavefront=0;
							}
							if(secs1[i].wavefront<=cellsec1-2){
							     if(trueSolution1[i].flux(secs1[i].Estimates.get(secs1[i].wavefront,0))-trueSolution1[i].flux(secs1[i].Estimates.get(secs1[i].wavefront+1,0))>0){								
								    secs1[i].wavedirection=0;							
						         }
						         else{
								    secs1[i].wavedirection=1;
						         }
							}
							else if (secs1[i].wavefront==cellsec1-1){
								if(secs1[i].index!=numSections1-1){
									if(trueSolution1[i].flux(secs1[i].Estimates.get(secs1[i].wavefront,0))-trueSolution1[i+1].flux(secs1[i].Estimates.get(overlap1,0))>0){										
										secs1[i].wavedirection=0;									
								    }
								    else{					
										secs1[i].wavedirection=1;									
								    }
								}
								else{
									secs1[i].wavedirection=0;
								}								
							}
							secs1[i].getModelA();
				 		    secs1[i].getModelB1();
						    secs1[i].getModelB2();						   						
						}
						else if (secs1[i].getClass().getSimpleName().equals("CF")){
							secs1[i].getwavefrontfromEstimation();
							secs1[i].getModelA();
				 		    secs1[i].getModelB1();
						    secs1[i].getModelB2();
						}					
					}
					
					for(int i=0;i<numSections2;i++){
						if(secs2[i].getClass().getSimpleName().equals("FC")){
							secs2[i].getwavefrontfromEstimation();
							if(secs2[i].wavefront<0){
								secs2[i].wavefront=0;
							}
							if(secs2[i].wavefront<=cellsec2-2){
							     if(trueSolution2[i].flux(secs2[i].Estimates.get(secs2[i].wavefront,0))-trueSolution2[i].flux(secs2[i].Estimates.get(secs2[i].wavefront+1,0))>0){							
								     secs2[i].wavedirection=0;							
						         }
						         else{						
								     secs2[i].wavedirection=1;						
						         }
							}
							else if (secs2[i].wavefront==cellsec2-1){
								if(secs2[i].index!=numSections2-1){
									if(trueSolution2[i].flux(secs2[i].Estimates.get(secs2[i].wavefront,0))-trueSolution2[i+1].flux(secs2[i].Estimates.get(overlap2,0))>0){										
										secs2[i].wavedirection=0;									
								    }
								    else{					
										secs2[i].wavedirection=1;									
								    }
								}
								else{
									secs2[i].wavedirection=0;
								}								
							}
							secs2[i].getModelA();
				 		    secs2[i].getModelB1();
						    secs2[i].getModelB2();						   						
						}
						else if (secs2[i].getClass().getSimpleName().equals("CF")){
							secs2[i].getwavefrontfromEstimation();
							secs2[i].getModelA();
				 		    secs2[i].getModelB1();
						    secs2[i].getModelB2();
						}						
					}					
					for (int i=0; i<numSections; i++) {
						estimations[i].setSection(secs[i]);
					}
					for (int i=0; i<numSections1; i++) {
						estimations1[i].setSection(secs1[i]);
					}
					for (int i=0; i<numSections2; i++) {
						estimations2[i].setSection(secs2[i]);
					}					
					for (int i=0; i<numSections; i++) {
						trueSolution[i].section=secs[i];
					}					
					for (int i=0; i<numSections1; i++) {
						trueSolution1[i].section=secs1[i];
					}
					for (int i=0; i<numSections2; i++) {
						trueSolution2[i].section=secs2[i];
					}
					for (int i=0; i<numSections; i++) {
						if (i==0){
							GammaWrite[i].write(estimations[i].filter.gammaStar+",");
						}
						else if (i==numSections-1){
							GammaWrite[i].write(estimations[i].filter.gammaStar+",");
						}
						else{
							GammaWrite[i].write(estimations[i].filter.gammaStar+",");
						}				
						GammaWrite[i].write("\n");
					}
					for (int i=0; i<numSections; i++) {
						Mode[i].write(estimations[i].roadModel.section.getClass().getSimpleName()+",");
						Mode[i].write(estimations[i].roadModel.section.wavefront+",");//estimated location of shock
						Mode[i].write(estimations[i].roadModel.section._wavefront+",");//true location of shock
						Mode[i].write(estimations[i].roadModel.section.wavedirection+",");
						Mode[i].write(estimations[i].roadModel.section._wavedirection+",");					
						Mode[i].write("\n");
					}
					for (int i=0; i<numSections1; i++) {
						Mode1[i].write(estimations1[i].roadModel.section.getClass().getSimpleName()+",");
						Mode1[i].write(estimations1[i].roadModel.section.wavefront+",");
						Mode1[i].write(estimations1[i].roadModel.section._wavefront+",");						
						Mode1[i].write(estimations1[i].roadModel.section.wavedirection+",");
						Mode1[i].write(estimations1[i].roadModel.section._wavedirection+",");
						Mode1[i].write("\n");
					}
					for (int i=0; i<numSections2; i++) {
						Mode2[i].write(estimations2[i].roadModel.section.getClass().getSimpleName()+",");
						Mode2[i].write(estimations2[i].roadModel.section.wavefront+",");
						Mode2[i].write(estimations2[i].roadModel.section._wavefront+",");					
						Mode2[i].write(estimations2[i].roadModel.section.wavedirection+",");
						Mode2[i].write(estimations2[i].roadModel.section._wavedirection+",");
						Mode2[i].write("\n");
					}
					
					for (int i=0; i<numSections; i++) {
//						trueSolution[i].updateMeasurement();
//						roadModels[i].updateMeasurement();
						estimations[i].filter.getNewParametersFromModel();
					}
					
					for (int i=0; i<numSections1; i++) {
//						trueSolution[i].updateMeasurement();
//						roadModels[i].updateMeasurement();
						estimations1[i].filter.getNewParametersFromModel();
					}
					
					for (int i=0; i<numSections2; i++) {
//						trueSolution[i].updateMeasurement();
//						roadModels[i].updateMeasurement();
						estimations2[i].filter.getNewParametersFromModel();
					}
					
					for (int i = 0; i<numSections; i++) {
						S[i]=(estimations[i].filter.measure.transpose()).mmul(InverseMatrix.invPoSym(estimations[i].filter.measureVar)).mmul(estimations[i].filter.measure);
					}					
										
					double disag=0;
					for (int i=0; i<numSections-1; i++) {
						disag= disag+(((mean[i].getRange(cellsec-overlap, cellsec,0,1).sub(mean[i+1].getRange(0, overlap,0,1))).transpose().mmul(mean[i].getRange(cellsec-overlap, cellsec,0,1).sub(mean[i+1].getRange(0, overlap,0,1)))).get(0,0))/((double)overlap);
					}
					disag=disag/((double)(numSections-1));
					writerDisagreement.write(disag+",");
					disagAvg[0]=disagAvg[0]+disag/((double)limite);
					for (int i=0; i<numSections-1; i++) {
						writerDisagreement.write(((mean[i].getRange(cellsec-overlap, cellsec,0,1).sub(mean[i+1].getRange(0, overlap,0,1))).transpose().mmul(mean[i].getRange(cellsec-overlap, cellsec,0,1).sub(mean[i+1].getRange(0, overlap,0,1)))).get(0,0)+",");
					}
					writerDisagreement.write("\n");
					
					double disag1=0;
					for (int i=0; i<numSections1-1; i++) {
						disag1= disag1+(((mean_1[i].getRange(cellsec1-overlap1, cellsec1,0,1).sub(mean_1[i+1].getRange(0, overlap1,0,1))).transpose().mmul(mean_1[i].getRange(cellsec1-overlap1, cellsec1,0,1).sub(mean_1[i+1].getRange(0, overlap1,0,1)))).get(0,0))/((double)overlap1);
					}
					disag1=disag1/((double)(numSections1-1));
					writerDisagreement1.write(disag1+",");
					disagAvg[1]=disagAvg[1]+disag1/((double)limite);
					for (int i=0; i<numSections1-1; i++) {
						writerDisagreement1.write(((mean_1[i].getRange(cellsec1-overlap1, cellsec1,0,1).sub(mean_1[i+1].getRange(0, overlap1,0,1))).transpose().mmul(mean_1[i].getRange(cellsec1-overlap1, cellsec1,0,1).sub(mean_1[i+1].getRange(0, overlap1,0,1)))).get(0,0)+",");
					}
					writerDisagreement1.write("\n");
					
					double disag2=0;
					for (int i=0; i<numSections2-1; i++) {
						disag2= disag2+(((mean_2[i].getRange(cellsec2-overlap2, cellsec2,0,1).sub(mean_2[i+1].getRange(0, overlap2,0,1))).transpose().mmul(mean_2[i].getRange(cellsec2-overlap2, cellsec2,0,1).sub(mean_2[i+1].getRange(0, overlap2,0,1)))).get(0,0))/((double)overlap2);
					}
					disag2=disag2/((double)(numSections2-1));
					writerDisagreement2.write(disag2+",");
					disagAvg[2]=disagAvg[2]+disag2/((double)limite);
					for (int i=0; i<numSections2-1; i++) {
						writerDisagreement2.write(((mean_2[i].getRange(cellsec2-overlap2, cellsec2,0,1).sub(mean_2[i+1].getRange(0, overlap2,0,1))).transpose().mmul(mean_2[i].getRange(cellsec2-overlap2, cellsec2,0,1).sub(mean_2[i+1].getRange(0, overlap2,0,1)))).get(0,0)+",");
					}
					writerDisagreement2.write("\n");
					
					trueSolutionCTM.update();

					//**start defining boundary condition
					int cells=trueSolutionCTM.trueStatesCTMPrior.getRows();
					if (bdry==0){
						double inflow=trueSolutionCTM.receiving(trueSolutionCTM.trueStatesCTMPrior.get(0,0));
						double outflow=trueSolutionCTM.sending(trueSolutionCTM.trueStatesCTMPrior.get(cells-1,0));
						double upOut=trueSolutionCTM.computeFlux(trueSolutionCTM.trueStatesCTMPrior.get(0,0),trueSolutionCTM.trueStatesCTMPrior.get(1,0));
						trueSolutionCTM.trueStatesCTM.put(0,0,trueSolutionCTM.trueStatesCTMPrior.get(0,0)+(trueSolutionCTM.dt/trueSolutionCTM.dx)*(inflow-upOut));

						double downIn=trueSolutionCTM.computeFlux(trueSolutionCTM.trueStatesCTMPrior.get(cells-2,0),trueSolutionCTM.trueStatesCTMPrior.get(cells-1,0));
						trueSolutionCTM.trueStatesCTM.put(cells-1,0,trueSolutionCTM.trueStatesCTMPrior.get(cells-1,0)+(trueSolutionCTM.dt/trueSolutionCTM.dx)*(downIn-outflow));
					}
					
					else if (bdry==1){
						double inflow=0.1125+0.1125*Math.sin(k*(Math.PI/4000)+(Math.PI));
						if (inflow>trueSolutionCTM.receiving(trueSolutionCTM.trueStatesCTMPrior.get(0,0))){
							inflow=trueSolutionCTM.receiving(trueSolutionCTM.trueStatesCTMPrior.get(0,0));
						}
						
						double outflow=trueSolutionCTM.receiving(trueSolutionCTM.trueStatesCTMPrior.get(cells-1,0));
//						double outflow=0.1125+0.1125*Math.sin(k*(Math.PI/1000));
//						if (outflow>trueSolution[numSections-1].sending(trueSolution[numSections-1].trueStatesPrior.get(cellsec-1,0))){
//							outflow=trueSolution[numSections-1].sending(trueSolution[numSections-1].trueStatesPrior.get(cellsec-1,0));
//						}
						
						double upOut=trueSolutionCTM.computeFlux(trueSolutionCTM.trueStatesCTMPrior.get(0,0),trueSolutionCTM.trueStatesCTMPrior.get(1,0));
						trueSolutionCTM.trueStatesCTM.put(0,0,trueSolutionCTM.trueStatesCTMPrior.get(0,0)+(trueSolutionCTM.dt/trueSolutionCTM.dx)*(inflow-upOut));

						double downIn=trueSolutionCTM.computeFlux(trueSolutionCTM.trueStatesCTMPrior.get(cells-2,0),trueSolutionCTM.trueStatesCTMPrior.get(cells-1,0));
						trueSolutionCTM.trueStatesCTM.put(cells-1,0,trueSolutionCTM.trueStatesCTMPrior.get(cells-1,0)+(trueSolutionCTM.dt/trueSolutionCTM.dx)*(downIn-outflow));
					}
					else{
						double inflow=0.1125+0.1125*Math.sin(k*(Math.PI/8000)+(Math.PI));
						if (inflow>trueSolutionCTM.receiving(trueSolutionCTM.trueStatesCTMPrior.get(0,0))){
							inflow=trueSolutionCTM.receiving(trueSolutionCTM.trueStatesCTMPrior.get(0,0));
						}
						
						double outflow=trueSolutionCTM.receiving(trueSolutionCTM.trueStatesCTMPrior.get(cells-1,0));
//						double outflow=0.1125+0.1125*Math.sin(k*(Math.PI/1000));
//						if (outflow>trueSolution[numSections-1].sending(trueSolution[numSections-1].trueStatesPrior.get(cellsec-1,0))){
//							outflow=trueSolution[numSections-1].sending(trueSolution[numSections-1].trueStatesPrior.get(cellsec-1,0));
//						}
						
						double upOut=trueSolutionCTM.computeFlux(trueSolutionCTM.trueStatesCTMPrior.get(0,0),trueSolutionCTM.trueStatesCTMPrior.get(1,0));
						trueSolutionCTM.trueStatesCTM.put(0,0,trueSolutionCTM.trueStatesCTMPrior.get(0,0)+(trueSolutionCTM.dt/trueSolutionCTM.dx)*(inflow-upOut));

						double downIn=trueSolutionCTM.computeFlux(trueSolutionCTM.trueStatesCTMPrior.get(cells-2,0),trueSolutionCTM.trueStatesCTMPrior.get(cells-1,0));
						trueSolutionCTM.trueStatesCTM.put(cells-1,0,trueSolutionCTM.trueStatesCTMPrior.get(cells-1,0)+(trueSolutionCTM.dt/trueSolutionCTM.dx)*(downIn-outflow));
						//*define boundary conditions here
					}
					//**end defining boundary condition
					
					trueSolutionCTM.getMeasurement();
					
					for(int i=0; i<numSections; i++){
						trueSolution[i].update();
					}
					
					for(int i=0; i<numSections; i++){
						trueSolution[i].updatemeasurement();
					}
					
					for(int i=0; i<numSections1; i++){
						trueSolution1[i].update();
					}
					
					for(int i=0; i<numSections1; i++){
						trueSolution1[i].updatemeasurement();
					}
					
					for(int i=0; i<numSections2; i++){
						trueSolution2[i].update();
					}
					
					for(int i=0; i<numSections2; i++){
						trueSolution2[i].updatemeasurement();
					}

					for (int i=0; i<numSections; i++) {
						estimations[i].nextStepWithoutUpdateTrue();
					}
					
					for (int i=0; i<numSections1; i++) {
						estimations1[i].nextStepWithoutUpdateTrue();
					}
					
					for (int i=0; i<numSections2; i++) {
						estimations2[i].nextStepWithoutUpdateTrue();
					}
					
					if (estimations[0].consensus){
						
						for (int i=0; i<numSections; i++) {
							estimations[i].filter.ComputeEig();
						}
						for (int i=0; i<numSections; i++) {
							if (i==0){
								estimations[i].filter.ComputeGammaStar2(estimations[1].filter);
							}
							else if (i==numSections-1){
								estimations[i].filter.ComputeGammaStar1(estimations[numSections-2].filter);
							}
							else{
								estimations[i].filter.ComputeGammaStar(estimations[i-1].filter,estimations[i+1].filter);
							}
							
						}
						
						DoubleMatrix[] mean1=new DoubleMatrix[numSections];
						mean1[0]=estimations[0].filter.Consensus2(estimations[1].filter);
						writerConsensusTerm.write(estimations[0].filter.scaling.get(0)+",");
						writerConsensusTerm.write(estimations[0].filter.scaling.get(1)+",");
						
						for (int i=1; i<numSections-1; i++) {
							mean1[i]=estimations[i].filter.Consensus(estimations[i-1].filter,estimations[i+1].filter);
							writerConsensusTerm.write(estimations[i].filter.scaling.get(0)+",");
							writerConsensusTerm.write(estimations[i].filter.scaling.get(1)+",");
						}

	                    mean1[numSections-1]=estimations[numSections-1].filter.Consensus1(estimations[numSections-2].filter);	
	                    writerConsensusTerm.write(estimations[numSections-1].filter.scaling.get(0)+",");
	                    writerConsensusTerm.write(estimations[numSections-1].filter.scaling.get(1)+",");
	                    writerConsensusTerm.write("\n");
						for (int i=0; i<numSections; i++) {
							estimations[i].filter.mean=mean1[i];
						}
					}
				}
				
				for (int i = 0; i<numSections; i++) {
					writerSection[i].flush(); writerSection[i].close();
					Mode[i].flush(); Mode[i].close();
					LyapSection[i].flush(); LyapSection[i].close();
					GammaWrite[i].flush(); GammaWrite[i].close();
				}
				for (int i = 0; i<numSections1; i++) {
					writerSection1[i].flush(); writerSection1[i].close();
					Mode1[i].flush(); Mode1[i].close();
				}
				for (int i = 0; i<numSections2; i++) {
					writerSection2[i].flush(); writerSection2[i].close();
					Mode2[i].flush(); Mode2[i].close();
				}
				writerAvr.flush(); writerAvr.close();
				writerConsensusTerm.flush(); writerConsensusTerm.close();
				writerAvr1.flush(); writerAvr1.close();
				writerAvr2.flush(); writerAvr2.close();
				Error.flush(); Error.close();
				Error1.flush(); Error1.close();
				Error2.flush(); Error2.close();
				Lyap.flush(); Lyap.close();
				writerTrue.flush(); writerTrue.close();
				writerDisagreement.flush(); writerDisagreement.close();
				writerDisagreement1.flush(); writerDisagreement1.close();
				writerDisagreement2.flush(); writerDisagreement2.close();
				
				System.out.print("Disag_avg_DLKCF="+disagAvg[0]+"\n");
				System.out.print("Disag_avg_DLKF="+disagAvg[1]+"\n");
				System.out.print("Disag_avg_LKF="+disagAvg[2]+"\n");
				System.out.print("Err_avg_DLKCF="+errorAvg[0]+"\n");
				System.out.print("Err_avg_DLKF="+errorAvg[1]+"\n");
				System.out.print("Err_avg_LKF="+errorAvg[2]+"\n");

				
				
				System.out.println(" End");						
		}
		catch (Exception e) {e.printStackTrace();}		
	}	
}

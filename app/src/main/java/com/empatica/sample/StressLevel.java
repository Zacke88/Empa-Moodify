package com.empatica.sample;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joakim on 2016-01-07.
 */
public class StressLevel {

    float N;
    float n;
    float SDNN;
    float RMSSD;
    int i;
    private List<Ibis> ibis = new ArrayList<>();
    private List<Ibis> selectedIbis = new ArrayList<>();
    private double timestamp = 0;
    double afterTimestamp = 0;
    double stressEstimate = 0;

    public void addIbi(float ibi, double timestamp) {
        Ibis tempIbi = new Ibis(ibi, timestamp);
        ibis.add(tempIbi);
        this.timestamp = timestamp;
    }

    // TODO KOLLA SÅ DETTA FUNKAR
    public double getStress() {
        afterTimestamp = timestamp - 60;
        Log.e("msg", Double.toString(timestamp));
        selectedIbis.clear();

        for (Ibis val : ibis) {
            if (val.getTimestamp() > afterTimestamp) {
                selectedIbis.add(val);
            }
        }
        Log.e("msg", Integer.toString(selectedIbis.size()));
        float mean_ibi = 0;
        float sum_ibi = 0;
        float sum_sqr_diff = 0;
        double rmssd_ibi = 0;
        float last_ibi = -1;

        if (selectedIbis.size() >= 20) {

            for (Ibis val : selectedIbis) {
                sum_ibi += val.getIbi();
                if (last_ibi > 0) {
                    sum_sqr_diff += (val.getIbi() - last_ibi) * (val.getIbi() - last_ibi);
                }
                last_ibi = val.getIbi();

            }
            mean_ibi = sum_ibi / (float) selectedIbis.size();
            rmssd_ibi = Math.sqrt((sum_sqr_diff) / ((float) (selectedIbis.size() - 1)));

            double std_ibi = 0;
            float sum_sqr_dev = 0;


            for (Ibis val : selectedIbis) {
                sum_sqr_dev += (val.getIbi() - mean_ibi) * (val.getIbi() - mean_ibi);
            }
            std_ibi = Math.sqrt(sum_sqr_dev / (float) (selectedIbis.size() - 1));

            stressEstimate = std_ibi / rmssd_ibi;
        }
        else {
            stressEstimate = 0;
        }

        return stressEstimate;
    }

    public class Ibis{

        float ibi;
        double timestamp;

        public Ibis(float ibi, double timestamp){
            this.ibi = ibi;
            this.timestamp = timestamp;
        }
        public float getIbi(){
            return ibi;
        }
        public double getTimestamp(){
            return timestamp;
        }
    }
}

package frc.robot;
public class Threshold {

    public static double getValue(double input, double threshold)
    {
        // If input's magnitude is less than threshold, return 0.
        // Otherwise return input.
        double inputAbs = Math.abs(input);
        if(inputAbs > threshold){
            return input;
        }
        else{
            return 0;
        }
    }
}

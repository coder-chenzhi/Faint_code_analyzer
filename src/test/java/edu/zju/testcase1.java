package edu.zju;

public class testcase1 {
     
    static int c=2;
    public static void main(String[] args)
    {
        int count=0;			
        int a=0,b=1,d=3;	
        int sum=0;			
        
        if(count==0)            
        {
         a=a+b;                 
         b=a+c;                 
         c=c*c;
         System.out.println(c);
        }
      
        sum=series_sum();      
        count=a;			
        System.out.println("Sum of series is : "+sum); 
    }
    
    public static int series_sum()
    {
        int sum=0;		
        for(int i=0;i<c;i++)
        {      
          sum=sum+i;  
        }                    
        return sum;          
    }

    public static void transitiveDead() {
        int a = 1, b = 2, c = 3;
        b = 2 * a;
        c = a + b;
    }
}

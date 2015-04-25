package org.solrcn.mapreduce.lire;

import com.google.common.hash.Hashing;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.LocalJobRunner;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

import java.io.IOException;

public class FeatureExport {

    private final static Logger log =Logger.getLogger(FeatureExport.class);

    public static class MapClass extends Mapper<LongWritable, Text, Text, Text> {

        private final static Text v2 = new Text();
//        private final static Text word = new Text();

        private ImageToDocument imageToDocument;
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            this.imageToDocument = new ImageToDocument();
        }

        public void map(LongWritable key, Text value, Context context) {
            String[] fields = value.toString().split("\t");
            if(fields.length!=3)
                return;
            StringBuilder sb = new StringBuilder();
            try{
                imageToDocument.getFseMessage(fields[0], fields[2]);
                sb.append(imageToDocument.getFseMessage(fields[0], fields[2]));
                String cid = fields[1];
                for (int i = 0; i < cid.length(); i+=3) {
                    sb.append("cat").append("\2").append(cid.substring(0, cid.length()-i)).append("\1");
                }
                sb.setCharAt(sb.length()-1,'\n');
                v2.set(sb.toString());
                context.write(value, v2);
                log.info(fields[2] + " .....done");
            }catch (Exception e){
                log.warn(fields[2] + " .....error",e);
            }


        }
    }

    public static class ReducerClass extends Reducer<Text, Text, Text, Text> {
        private Text result = new Text();

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            context.write(key, result); //context.write(k3,v3)
        }
    }

    public static class PartitionClass extends Partitioner<Text,IntWritable> {

        @Override
        public int getPartition(Text text, IntWritable intWritable, int numPartitions) {
            return (Hashing.murmur3_32().hashBytes(text.getBytes()).asInt()&Integer.MAX_VALUE) % numPartitions;
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
//        args = new String[]{"d:/tmp/hadoop/input", "d:/tmp/hadoop/output_" + System.currentTimeMillis()};
        if (args.length < 2) {
            System.err.println("Usage: ImagesFeatureExport <in> <out>");
            System.exit(2);
        }
        Path input = new Path(args[0]);
        Path output = new Path(args[1]);
//        Job job = new Job(conf, "keywords count");              //旧写法
        Job job = Job.getInstance(conf);                        //新写法
        job.setJobName("ImagesFeatureExport");
//        LocalJobRunner.setLocalMaxRunningMaps(job, 2);          //本地调试控制线程数
        long maxInputSplitSize = 10*1024*1024;
        long minInputSplitSize = 1*1024*1024;
        FileInputFormat.setMinInputSplitSize(job,1024);
        if(args.length>2){
            try{
                minInputSplitSize = Integer.parseInt(args[2]);
                FileInputFormat.setMaxInputSplitSize(job,minInputSplitSize);
                if(args.length>3){
                    maxInputSplitSize = Integer.parseInt(args[3]);
                    FileInputFormat.setMaxInputSplitSize(job,maxInputSplitSize);
                }
            }catch (NumberFormatException e){
                log.error(e);
            }

        } else {
            FileInputFormat.setMaxInputSplitSize(job,10*1024*1024);
        }
        log.info("maxInputSplitSize:" + maxInputSplitSize);
        if(FileSystem.get(conf).exists(output));
        FileSystem.get(conf).delete(output,true);
        FileInputFormat.addInputPath(job, input);   //设置数据输入路径
        FileOutputFormat.setOutputPath(job, output); //设置数据输出路径
        job.setNumReduceTasks(0);                               //设置reduce数量
        job.setJarByClass(FeatureExport.class);
        job.setMapperClass(MapClass.class);
//        job.setPartitionerClass(PartitionClass.class);
//        job.setCombinerClass(ReducerClass.class);
        job.setReducerClass(ReducerClass.class);
        job.setInputFormatClass(TextInputFormat.class);         //K1,V1->lineNumber,text
        job.setMapOutputKeyClass(Text.class);                   //K2
        job.setMapOutputValueClass(Text.class);                 //V2
        job.setOutputKeyClass(Text.class);                      //K3
        job.setOutputValueClass(Text.class);                    //V3
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

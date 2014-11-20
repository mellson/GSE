using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using System.Drawing;
using Emgu.CV.CvEnum;
using Emgu.CV;
using Emgu.CV.Structure;

namespace ApproximatorClient.Sensors
{
    class FaceDetectionSensor : AbstractSensor
    {
        Thread faceDetectionThread;
        public FaceDetectionSensor(string username, string name = "FaceDetectionSensor")
        {
            SensorName = name;
            UserName = username;
            UpdateReadingForSensorName(SensorName, UserName, "Init");
            faceDetectionThread = new Thread(DetectFaces);
            faceDetectionThread.Start();
            StartUploading();
        }

        public void DetectFaces()
        {
            var face = new HaarCascade("Resources/haarcascade_frontalface_default.xml");
            var grabber = new Capture();
            grabber.QueryFrame();
            Image<Bgr, Byte> currentFrame;
            Image<Gray, byte> gray = null;
            Image<Gray, byte> result = null;
            MCvAvgComp[][] facesDetected;
            String win1 = "Test Window";
            while (true) 
            {
                currentFrame = grabber.QueryFrame().Resize(320, 240, Emgu.CV.CvEnum.INTER.CV_INTER_CUBIC);
                gray = currentFrame.Convert<Gray, Byte>();
                facesDetected = gray.DetectHaarCascade(
                      face,
                      1.2,
                      10,
                      Emgu.CV.CvEnum.HAAR_DETECTION_TYPE.DO_CANNY_PRUNING,
                      new Size(20, 20));
                Console.WriteLine("Faces detected: {0}", facesDetected[0].Length);
                foreach (MCvAvgComp f in facesDetected[0])
                {
                    result = currentFrame.Copy(f.rect).Convert<Gray, byte>().Resize(100, 100, Emgu.CV.CvEnum.INTER.CV_INTER_CUBIC);
                    currentFrame.Draw(f.rect, new Bgr(Color.Red), 2);
                }
                //CvInvoke.cvShowImage(win1, currentFrame.Ptr);
                //CvInvoke.cvWaitKey(0);
                UpdateReadingForSensorName(SensorName, UserName, facesDetected[0].Length.ToString());
                Thread.Sleep(300);
            }
        } 
    }
}

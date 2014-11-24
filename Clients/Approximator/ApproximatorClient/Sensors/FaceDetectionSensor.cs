using System;
using System.Globalization;
using System.Threading;
using System.Drawing;
using Emgu.CV.CvEnum;
using Emgu.CV;
using Emgu.CV.Structure;

namespace ApproximatorClient.Sensors
{
    class FaceDetectionSensor : AbstractSensor
    {
        public FaceDetectionSensor(string username, string name = "FaceDetectionSensor")
        {
            SensorName = name;
            UserName = username;
            UpdateReadingForSensorName(SensorName, UserName, "Init");
            var faceDetectionThread = new Thread(DetectFaces);
            faceDetectionThread.Start();
            StartUploading();
        }

        public void DetectFaces()
        {
            var face = new HaarCascade("OpenCV/haarcascade_frontalface_default.xml");
            var grabber = new Capture(1);
            grabber.QueryFrame();
            while (true)
            {
                var currentFrame = grabber.QueryFrame();
                var gray = currentFrame.Convert<Gray, Byte>();
                var facesDetected = gray.DetectHaarCascade(face);
                Console.WriteLine(@"Faces detected: {0}", facesDetected[0].Length);
                UpdateReadingForSensorName(SensorName, UserName, facesDetected[0].Length.ToString(CultureInfo.InvariantCulture));
                Thread.Sleep(300);
            }
        } 
    }
}

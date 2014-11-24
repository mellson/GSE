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
            var grabber = new Capture();
            grabber.QueryFrame();
            while (true) 
            {
                var currentFrame = grabber.QueryFrame().Resize(320, 240, INTER.CV_INTER_CUBIC);
                var gray = currentFrame.Convert<Gray, Byte>();
                var facesDetected = gray.DetectHaarCascade(
                    face,
                    1.2,
                    10,
                    HAAR_DETECTION_TYPE.DO_CANNY_PRUNING,
                    new Size(20, 20));
                Console.WriteLine(@"Faces detected: {0}", facesDetected[0].Length);
                foreach (var f in facesDetected[0])
                {
                    currentFrame.Copy(f.rect).Convert<Gray, byte>().Resize(100, 100, INTER.CV_INTER_CUBIC);
                    currentFrame.Draw(f.rect, new Bgr(Color.Red), 2);
                }
                UpdateReadingForSensorName(SensorName, UserName, facesDetected[0].Length.ToString(CultureInfo.InvariantCulture));
                Thread.Sleep(300);
            }
        } 
    }
}

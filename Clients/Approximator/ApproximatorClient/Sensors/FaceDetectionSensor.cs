using System;
using System.Linq;
using System.Threading;
using Emgu.CV;
using Emgu.CV.Structure;
using WebSocketSharp;

namespace ApproximatorClient.Sensors
{
    class FaceDetectionSensor : AbstractSensor
    {
        public FaceDetectionSensor(string username, WebSocket webSocket, string name = "FaceDetectionSensor", int cameraIndex = 0)
        {
            SensorName = name;
            UserName = username;
            CameraIndex = cameraIndex;
            WebSocket = webSocket;
            UpdateReadingForSensorName(SensorName, UserName, "Init");
            var faceDetectionThread = new Thread(DetectFaces) {IsBackground = true};
            faceDetectionThread.Start();
            StartUploading();
        }

        public void DetectFaces()
        {
            var frontalFace = new HaarCascade("OpenCV/haarcascade_frontalface_default.xml");
            var profileFace = new HaarCascade("OpenCV/haarcascade_profileface.xml");
            var grabber = new Capture(CameraIndex);
            while (true)
            {
                var currentFrame = grabber.QueryFrame();
                var gray = currentFrame.Convert<Gray, Byte>();
                var facesDetected = gray.DetectHaarCascade(frontalFace);
                var userInFrontOfScreen = facesDetected[0].Any();
                var statusString = "Not Present";
                if (userInFrontOfScreen)
                    statusString = "Present with frontal face";
                else
                {
                    facesDetected = gray.DetectHaarCascade(profileFace);
                    userInFrontOfScreen = facesDetected[0].Any();
                    statusString = userInFrontOfScreen ? "Present with profile face" : statusString;
                }
                UpdateReadingForSensorName(SensorName, UserName, statusString);
                Thread.Sleep(500);
            }
        }

        public int CameraIndex { get; set; }
    }
}

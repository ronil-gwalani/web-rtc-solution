package org.ron.webrtccall.server

fun getHealthCheckHtml(): String = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WebRTC Signaling Server - Status</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;700&display=swap');
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Poppins', sans-serif;
        }

        body {
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            overflow: hidden;
        }

        .container {
            text-align: center;
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(15px);
            padding: 3rem;
            border-radius: 20px;
            border: 1px solid rgba(255, 255, 255, 0.2);
            box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
            animation: fadeIn 1.5s ease-out;
        }

        h1 {
            color: #ffffff;
            font-size: 3.5rem;
            font-weight: 700;
            margin-bottom: 1rem;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
        }

        .status-badge {
            display: inline-block;
            background: #4ade80;
            color: #14532d;
            padding: 0.5rem 1.5rem;
            border-radius: 50px;
            font-weight: 600;
            font-size: 0.9rem;
            margin-bottom: 2rem;
            box-shadow: 0 0 15px rgba(74, 222, 128, 0.4);
        }

        p {
            color: rgba(255, 255, 255, 0.8);
            font-size: 1.2rem;
            margin-top: 1rem;
        }

        .heart {
            color: #ff4d4d;
            display: inline-block;
            animation: pulse 1s infinite;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }

        @keyframes pulse {
            0% { transform: scale(1); }
            50% { transform: scale(1.2); }
            100% { transform: scale(1); }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="status-badge">● Server is Online</div>
        <h1>You are the best Ronil!</h1>
        <p>Your WebRTC Signaling Server is running smoothly <span class="heart">❤</span></p>
    </div>
</body>
</html>
""".trimIndent()

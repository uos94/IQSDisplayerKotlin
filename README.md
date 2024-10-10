# Android Example Application

기존 Java코드를 수정하여 최신화 하였음.
전체적으로 코드 분량이 절반가량 줄어들었으며, 로직을 통합하며 가독성을 개선함
Apk용량이 1Mb정도 줄어들었음


## 개선해야 할 점
- Storage의 최상위 폴더는 Android 11부터 App단에서 직접적인 접근이 불가능함. 또한 공용 저장소의 경우 Scoped Storage가 추가되면서 런타임에 권한을 획득해야함.  
- 각각의 로직이 여러 사람을 거쳐가면서 로직이 복잡하고, 중복되는 변수가 많아 가독성이 떨어지고 유지보수가 어려움
- 사용하지 않는 코드가 많아 정작 분석해야 할 코드를 찾기 어려움...비효율적 코드도 많음..
- 서비스 클래스를 따로 두어 통신을 하는데, 서비스와의 통신을 위해 Activity이동 시 중복 코드가 발생하며, 서비스를 두번 접속하는 로직이 생김
- 5초마다 서비스 process가 살아있는지 확인하면서 없을 경우 강제 시작하는 코드가 있어, 최초 시작이나 업데이트 시 문제가 발생하는 경우가 생김
- Packet관련 코드가 각각의 패킷마다 분산되어 있어 유지보수가 어려움(중복코드, 일관성 결여)
- xml 코드가 비효율적으로 되어 있음.(미리보기가 즉각적이지 않음, 폰트적용을 컨트롤러 단에서 함, 기타등등)

## 변경된 점
1. 앱 재시작 관련 코드 삭제
   - **Device 부팅 시** BroadCast를 감지하여 자동으로 재시작 하도록 변경함
   - **앱 업데이트 시** 업데이트 관련 BroadCast를 감지하여 자동으로 재시작 하도록 변경
   - **알 수 없는 오류로 Exception발생 시** ExceptionHandler를 Application클래스에 등록하여 앱 재시작 하도록 변경함
   - 위의 로직에 따라 기존에 있던 서비스 process 감지 Thread를 삭제함. 
   - 위의 로직에 따라 앱을 강제로 재시작하던 Watch앱 불필요(기존에는 Watch앱으로 인해 앱 시작 시 두번 뜨는 이슈가 있었음)
   
2. Packet 데이터를 파싱할 하고 생성하는 코드들이 모든 패킷마다 따로 작업되어 있어 PacketAnalyzer class로 통합, 일관된 로직을 통과하도록 수정함. 
   
3. 로그Viewer 추가함
   - Log를 보고 싶어도 PC를 연결해야만 볼 수 있었기 때문에 MainFragment에서 Dest번호를 Long click하면 로그화면을 보여주도록 함.
   - 기존 Log를 File로 남기는 부분이 나누어져 있어. Log부분과 File로 저장하는 부분을 통합함.
   - Log 클래스 개선으로 어떤 클래스명과 Line 수 확인가능하도록 함.

4. TimerHandler를 사용하면서 타이밍 적인 이슈들이 생기고 불필요 변수들이 많아져 Handler자체를 삭제하고 FragmentFactory로 분리함. 
5. 서비스 클래스 삭제
   - 서비스 클래스의 목적이 네트워크 통신 때문인것으로 판단하고 TcpClient클래스로 통신만을 하도록 수정함
   - 서비스 클래스와 데이터를 주고 받기 위해 동일한 목적의 Receiver가 두개의 Activity에 존재하던 것을 삭제함
   - Activity이동 시 서비스를 중지하고 재시작 하던 부분을 수정함
   
6. 의미가 불분명하거나 불필요한 변수, 클래스 모두 삭제 및 통합
   - 클래스(WriteLog, ScreenInfo, AppInfo, Define..기타등등), 변수는 많이 통합함..

7. 기타 등등 수정함.
   - Apk설치방식 변경.
   - 불필요 라이브러리 삭제(autoscrollviewpager)
   - 단일Activity에 Fragment전환 방식으로 변경.
   - 대부분의 코드에 주석 추가로 mouse over로 확인 할 수 있도록 함.

## 추가 확인해야 할 사항
   - 일반적인 동작은 거의 대부분 확인하였으나, 개발자 혼자 테스트 해본것이라 모르는 버그가 있을 수 있음.
   - data클래스의 추가 정리 필요
   - Android 11을 대응하여 권한관련 로직을 수정하였으나 전혀 확인을 해보지 못하였음. 
   - 또한 실제 Android11용 루팅된 Device가 있다면 해볼만한 방법이 있으나 가능여부는 Test를 해봐야 알 수 있음. 

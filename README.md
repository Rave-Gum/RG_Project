# Rain_Bot (Discord ChatBot)

![language](https://img.shields.io/github/languages/top/Rave-Gum/RG_Project)
![version](https://img.shields.io/github/v/tag/Rave-Gum/RG_Project?label=last%20version)
![develop](https://img.shields.io/badge/develop-pause-yellow)

<img align = "right" src="https://user-images.githubusercontent.com/47655983/120768831-83dc1f80-c557-11eb-9984-314ca72718a7.png" width="300">


## 개요

디스코드 서버의 편의성 증진을 위해 개발중인 디스코드 봇입니다.

현재 4개의 기능이 구현되어 있으며 개발 완료시 배포될 예정입니다.


## 목차

1. [음악 재생](#음악-재생)

2. [도박](#도박)

3. [단어 검색](#단어-검색)

4. [투표](#투표)



## 음악 재생
> 재생가능한 링크를 추가하여 디스코드 음성채널에서 음악을 재생할 수 있습니다.



<strong>재생가능한 포멧</strong>

- YouTube
- SoundCloud
- BandCamp
- Vimeo
- Twitch Streams
- Local Files
- HTTP URLs

<strong>명령어</strong>

- `!재생 [음악링크]` - 음악을 재생목록에 추가합니다. 현재 재생중인 음악이 없으면 바로 재생합니다. (사용자가 음성채널에 접속중이어야 함)
- `!스킵` - 현재 재생중인 음악을 스킵하고 다음 음악을 재생합니다.
- `!정지` - 현재 재생중인 음악을 정지하고 재생목록에서 제거합니다.
- `!재생목록` - 재생목록을 출력합니다.
- `!일시정지` - 재생중인 음악을 일시정지합니다. 재생목록에는 남아있습니다.
- `!다시재생` - 일시정지 된 음악을 다시 재생합니다.



## 도박
> 간단한 도박을 즐길수 있습니다.



<strong>명령어</strong>

- `!돈` - 현재 자신이 가진 돈을 확인합니다.
- `!초기돈` - 초기 설정된 돈을 줍니다. (기본값 : 10만원)
- `!도박` - 단계별로 설정된 배율, 성공확률을 보여줍니다.
- `!배팅 [배팅할 돈] [단계]` - 돈을 배팅합니다. 결과는 바로 나옵니다.



## 단어 검색

> 원하는 단어를 검색할 수 있습니다.



<strong>명령어</strong>

- `!단어 [단어]` - 단어를 위키피디아에서 검색합니다.




## 투표

> 투표 제목과 후보지를 입력하여 투표를 진행할 수 있습니다.



<strong>명령어</strong>

- `!투표 투표제목/후보1/후보2[/후보3/후보4....]` - 입력한 후보지들을 가진 투표가 생성됩니다.
  - 후보지는 최소 2개 이상이어야 합니다.

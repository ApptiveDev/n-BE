# 채팅 API 명세서

## 1. 채팅방 목록 조회

> 본인이 참여한 모든 채팅방 목록을 조회합니다.

---

### 📌 Request

**Method:** `GET`  
**URL:** `/api/chat/rooms`

#### Request Header

| 이름 | 설명 | 필수 |
| --- | --- | --- |
| Authorization | 액세스 토큰 기반 인증 (Bearer {ACCESS_TOKEN}) | ✅ |

---

### 📌 Response

#### Response Fields

| 이름 | 설명 | Type | 기타 |
| --- | --- | --- | --- |
| chatRooms | 채팅방 목록 | Array |  |
| chatRooms[].chatRoomId | 채팅방 ID | Long |  |
| chatRooms[].matchingId | 매칭 ID | Long |  |
| chatRooms[].partner | 상대방 정보 | Object |  |
| chatRooms[].partner.memberId | 상대방 회원 ID | Long |  |
| chatRooms[].partner.name | 상대방 이름 | String |  |
| chatRooms[].partner.thumbnailImageUrl | 상대방 썸네일 이미지 URL | String | nullable |
| chatRooms[].partner.gender | 상대방 성별 | String | MALE, FEMALE |
| chatRooms[].lastMessage | 마지막 메시지 정보 | Object | nullable |
| chatRooms[].lastMessage.messageId | 메시지 ID | Long |  |
| chatRooms[].lastMessage.content | 메시지 원문 내용 | String |  |
| chatRooms[].lastMessage.translatedContent | 메시지 번역 내용 | String | nullable (번역 중이거나 실패 시 null) |
| chatRooms[].lastMessage.language | 메시지 언어 | String | KOREAN, JAPANESE |
| chatRooms[].lastMessage.senderId | 발신자 ID | Long |  |
| chatRooms[].lastMessage.messageType | 메시지 타입 | String | TEXT, IMAGE, SYSTEM |
| chatRooms[].lastMessage.createdAt | 메시지 생성 시간 | String | ISO 8601 형식 |
| chatRooms[].unreadCount | 읽지 않은 메시지 개수 | Integer |  |
| chatRooms[].createdAt | 채팅방 생성 시간 | String | ISO 8601 형식 |

#### Response

```json
HTTP/1.1 200 Ok
Content-Type: application/json

{
    "chatRooms": [
        {
            "chatRoomId": 1,
            "matchingId": 5,
            "partner": {
                "memberId": 2,
                "name": "사쿠라",
                "thumbnailImageUrl": "https://example.com/image.jpg",
                "gender": "FEMALE"
            },
            "lastMessage": {
                "messageId": 10,
                "content": "안녕하세요",
                "translatedContent": "こんにちは",
                "language": "KOREAN",
                "senderId": 1,
                "messageType": "TEXT",
                "createdAt": "2026-01-31T10:30:00"
            },
            "unreadCount": 3,
            "createdAt": "2026-01-30T09:00:00"
        },
        {
            "chatRoomId": 2,
            "matchingId": 6,
            "partner": {
                "memberId": 3,
                "name": "유키",
                "thumbnailImageUrl": null,
                "gender": "FEMALE"
            },
            "lastMessage": null,
            "unreadCount": 0,
            "createdAt": "2026-01-31T11:00:00"
        }
    ]
}
```

#### ⚠️ 예외 상황

| 상황 | 응답코드 | 메시지 |
| --- | --- | --- |
| 액세스 토큰 권한 잘못됨 | 401 | 권한이 없습니다. |

#### 💻 클라이언트 사용 예시

**JavaScript (Fetch API)**
```javascript
async function getChatRoomList(accessToken) {
    try {
        const response = await fetch('http://localhost:8080/api/chat/rooms', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('채팅방 목록:', data.chatRooms);
        
        // UI 업데이트
        data.chatRooms.forEach(room => {
            console.log(`채팅방 ID: ${room.chatRoomId}, 상대방: ${room.partner.name}, 읽지 않은 메시지: ${room.unreadCount}`);
        });
        
        return data;
    } catch (error) {
        console.error('채팅방 목록 조회 실패:', error);
        throw error;
    }
}

// 사용 예시
const token = localStorage.getItem('accessToken');
getChatRoomList(token);
```

**TypeScript (Axios)**
```typescript
import axios from 'axios';

interface ChatRoomListResponse {
    chatRooms: Array<{
        chatRoomId: number;
        matchingId: number;
        partner: {
            memberId: number;
            name: string;
            thumbnailImageUrl: string | null;
            gender: string;
        };
        lastMessage: {
            messageId: number;
            content: string;
            translatedContent: string | null;
            language: string;
            senderId: number;
            messageType: string;
            createdAt: string;
        } | null;
        unreadCount: number;
        createdAt: string;
    }>;
}

async function getChatRoomList(accessToken: string): Promise<ChatRoomListResponse> {
    const response = await axios.get<ChatRoomListResponse>(
        'http://localhost:8080/api/chat/rooms',
        {
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        }
    );
    return response.data;
}
```

**React Hook 예시**
```typescript
import { useState, useEffect } from 'react';

function useChatRoomList(accessToken: string) {
    const [chatRooms, setChatRooms] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function fetchChatRooms() {
            try {
                setLoading(true);
                const response = await fetch('http://localhost:8080/api/chat/rooms', {
                    headers: {
                        'Authorization': `Bearer ${accessToken}`
                    }
                });
                
                if (!response.ok) throw new Error('채팅방 목록 조회 실패');
                
                const data = await response.json();
                setChatRooms(data.chatRooms);
            } catch (err) {
                setError(err);
            } finally {
                setLoading(false);
            }
        }
        
        fetchChatRooms();
    }, [accessToken]);

    return { chatRooms, loading, error };
}
```

---

## 2. 채팅방 상세 조회

> 특정 채팅방의 상세 정보를 조회합니다.

---

### 📌 Request

**Method:** `GET`  
**URL:** `/api/chat/rooms/{chatRoomId}`

#### Path Parameters

| 이름 | 설명 | 필수 |
| --- | --- | --- |
| chatRoomId | 채팅방 ID | ✅ |

#### Request Header

| 이름 | 설명 | 필수 |
| --- | --- | --- |
| Authorization | 액세스 토큰 기반 인증 (Bearer {ACCESS_TOKEN}) | ✅ |

---

### 📌 Response

#### Response Fields

| 이름 | 설명 | Type | 기타 |
| --- | --- | --- | --- |
| chatRoomId | 채팅방 ID | Long |  |
| matchingId | 매칭 ID | Long |  |
| partner | 상대방 정보 | Object |  |
| partner.memberId | 상대방 회원 ID | Long |  |
| partner.name | 상대방 이름 | String |  |
| partner.thumbnailImageUrl | 상대방 썸네일 이미지 URL | String | nullable |
| partner.gender | 상대방 성별 | String | MALE, FEMALE |
| createdAt | 채팅방 생성 시간 | String | ISO 8601 형식 |

#### Response

```json
HTTP/1.1 200 Ok
Content-Type: application/json

{
    "chatRoomId": 1,
    "matchingId": 5,
    "partner": {
        "memberId": 2,
        "name": "사쿠라",
        "thumbnailImageUrl": "https://example.com/image.jpg",
        "gender": "FEMALE"
    },
    "createdAt": "2026-01-30T09:00:00"
}
```

#### ⚠️ 예외 상황

| 상황 | 응답코드 | 메시지 |
| --- | --- | --- |
| 액세스 토큰 권한 잘못됨 | 401 | 권한이 없습니다. |
| 채팅방을 찾을 수 없음 | 404 | 채팅방을 찾을 수 없습니다. |
| 해당 채팅방에 접근할 수 없음 | 403 | 해당 채팅방에 접근할 수 없습니다. |

#### 💻 클라이언트 사용 예시

**JavaScript (Fetch API)**
```javascript
async function getChatRoomDetail(chatRoomId, accessToken) {
    try {
        const response = await fetch(`http://localhost:8080/api/chat/rooms/${chatRoomId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('채팅방을 찾을 수 없습니다.');
            } else if (response.status === 403) {
                throw new Error('해당 채팅방에 접근할 수 없습니다.');
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('채팅방 상세:', data);
        return data;
    } catch (error) {
        console.error('채팅방 상세 조회 실패:', error);
        throw error;
    }
}

// 사용 예시
const token = localStorage.getItem('accessToken');
getChatRoomDetail(1, token);
```

---

## 3. 메시지 목록 조회

> 특정 채팅방의 메시지 목록을 페이징하여 조회합니다.

---

### 📌 Request

**Method:** `GET`  
**URL:** `/api/chat/rooms/{chatRoomId}/messages`

#### Path Parameters

| 이름 | 설명 | 필수 |
| --- | --- | --- |
| chatRoomId | 채팅방 ID | ✅ |

#### Query Parameters

| 이름 | 설명 | 필수 | 기본값 |
| --- | --- | --- | --- |
| page | 페이지 번호 (0부터 시작) | ❌ | 0 |
| size | 페이지 크기 | ❌ | 20 |
| sort | 정렬 기준 (createdAt,desc) | ❌ | createdAt,desc |

#### Request Header

| 이름 | 설명 | 필수 |
| --- | --- | --- |
| Authorization | 액세스 토큰 기반 인증 (Bearer {ACCESS_TOKEN}) | ✅ |

---

### 📌 Response

#### Response Fields

| 이름 | 설명 | Type | 기타 |
| --- | --- | --- | --- |
| messages | 메시지 목록 | Array | 최신순 정렬 |
| messages[].messageId | 메시지 ID | Long |  |
| messages[].chatRoomId | 채팅방 ID | Long |  |
| messages[].senderId | 발신자 ID | Long |  |
| messages[].senderName | 발신자 이름 | String |  |
| messages[].content | 메시지 원문 내용 | String |  |
| messages[].translatedContent | 메시지 번역 내용 | String | nullable (번역 중이거나 실패 시 null) |
| messages[].language | 메시지 언어 | String | KOREAN, JAPANESE |
| messages[].messageType | 메시지 타입 | String | TEXT, IMAGE, SYSTEM |
| messages[].isRead | 읽음 여부 | Boolean |  |
| messages[].readAt | 읽은 시간 | String | nullable, ISO 8601 형식 |
| messages[].createdAt | 메시지 생성 시간 | String | ISO 8601 형식 |
| page | 페이징 정보 | Object |  |
| page.number | 현재 페이지 번호 | Integer | 0부터 시작 |
| page.size | 페이지 크기 | Integer |  |
| page.totalElements | 전체 메시지 개수 | Long |  |
| page.totalPages | 전체 페이지 수 | Integer |  |
| page.hasNext | 다음 페이지 존재 여부 | Boolean |  |
| page.hasPrevious | 이전 페이지 존재 여부 | Boolean |  |

#### Response

```json
HTTP/1.1 200 Ok
Content-Type: application/json

{
    "messages": [
        {
            "messageId": 10,
            "chatRoomId": 1,
            "senderId": 1,
            "senderName": "김태윤",
            "content": "안녕하세요",
            "translatedContent": "こんにちは",
            "language": "KOREAN",
            "messageType": "TEXT",
            "isRead": true,
            "readAt": "2026-01-31T10:31:00",
            "createdAt": "2026-01-31T10:30:00"
        },
        {
            "messageId": 9,
            "chatRoomId": 1,
            "senderId": 2,
            "senderName": "사쿠라",
            "content": "こんにちは",
            "translatedContent": "안녕하세요",
            "language": "JAPANESE",
            "messageType": "TEXT",
            "isRead": true,
            "readAt": "2026-01-31T10:30:30",
            "createdAt": "2026-01-31T10:30:15"
        },
        {
            "messageId": 8,
            "chatRoomId": 1,
            "senderId": 1,
            "senderName": "김태윤",
            "content": "반가워요",
            "translatedContent": null,
            "language": "KOREAN",
            "messageType": "TEXT",
            "isRead": false,
            "readAt": null,
            "createdAt": "2026-01-31T10:29:00"
        }
    ],
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 15,
        "totalPages": 1,
        "hasNext": false,
        "hasPrevious": false
    }
}
```

#### ⚠️ 예외 상황

| 상황 | 응답코드 | 메시지 |
| --- | --- | --- |
| 액세스 토큰 권한 잘못됨 | 401 | 권한이 없습니다. |
| 채팅방을 찾을 수 없음 | 404 | 채팅방을 찾을 수 없습니다. |
| 해당 채팅방에 접근할 수 없음 | 403 | 해당 채팅방에 접근할 수 없습니다. |

#### 💻 클라이언트 사용 예시

**JavaScript (Fetch API)**
```javascript
async function getMessages(chatRoomId, accessToken, page = 0, size = 20) {
    try {
        const url = new URL(`http://localhost:8080/api/chat/rooms/${chatRoomId}/messages`);
        url.searchParams.append('page', page);
        url.searchParams.append('size', size);
        url.searchParams.append('sort', 'createdAt,desc');
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('메시지 목록:', data.messages);
        console.log('페이징 정보:', data.page);
        
        // 메시지를 시간순으로 정렬 (최신순)
        const sortedMessages = data.messages.sort((a, b) => 
            new Date(b.createdAt) - new Date(a.createdAt)
        );
        
        return data;
    } catch (error) {
        console.error('메시지 목록 조회 실패:', error);
        throw error;
    }
}

// 사용 예시 - 첫 페이지 조회
const token = localStorage.getItem('accessToken');
getMessages(1, token, 0, 20).then(data => {
    // 다음 페이지가 있는지 확인
    if (data.page.hasNext) {
        console.log('다음 페이지가 있습니다.');
    }
});
```

**React Hook 예시 (무한 스크롤)**
```typescript
import { useState, useEffect, useCallback } from 'react';

function useMessages(chatRoomId: number, accessToken: string) {
    const [messages, setMessages] = useState([]);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [loading, setLoading] = useState(false);

    const loadMessages = useCallback(async (pageNum: number) => {
        if (loading) return;
        
        setLoading(true);
        try {
            const response = await fetch(
                `http://localhost:8080/api/chat/rooms/${chatRoomId}/messages?page=${pageNum}&size=20&sort=createdAt,desc`,
                {
                    headers: {
                        'Authorization': `Bearer ${accessToken}`
                    }
                }
            );
            
            const data = await response.json();
            
            if (pageNum === 0) {
                setMessages(data.messages);
            } else {
                setMessages(prev => [...prev, ...data.messages]);
            }
            
            setHasMore(data.page.hasNext);
        } catch (error) {
            console.error('메시지 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    }, [chatRoomId, accessToken, loading]);

    const loadMore = useCallback(() => {
        if (hasMore && !loading) {
            const nextPage = page + 1;
            setPage(nextPage);
            loadMessages(nextPage);
        }
    }, [page, hasMore, loading, loadMessages]);

    useEffect(() => {
        loadMessages(0);
    }, [chatRoomId]);

    return { messages, loadMore, hasMore, loading };
}
```

---

## 4. 읽지 않은 메시지 개수 조회

> 본인이 읽지 않은 메시지의 총 개수와 채팅방별 개수를 조회합니다.

---

### 📌 Request

**Method:** `GET`  
**URL:** `/api/chat/rooms/unread-count`

#### Request Header

| 이름 | 설명 | 필수 |
| --- | --- | --- |
| Authorization | 액세스 토큰 기반 인증 (Bearer {ACCESS_TOKEN}) | ✅ |

---

### 📌 Response

#### Response Fields

| 이름 | 설명 | Type | 기타 |
| --- | --- | --- | --- |
| totalUnreadCount | 전체 읽지 않은 메시지 개수 | Integer |  |
| unreadCountByRoom | 채팅방별 읽지 않은 메시지 개수 | Array |  |
| unreadCountByRoom[].chatRoomId | 채팅방 ID | Long |  |
| unreadCountByRoom[].unreadCount | 읽지 않은 메시지 개수 | Integer |  |

#### Response

```json
HTTP/1.1 200 Ok
Content-Type: application/json

{
    "totalUnreadCount": 5,
    "unreadCountByRoom": [
        {
            "chatRoomId": 1,
            "unreadCount": 3
        },
        {
            "chatRoomId": 2,
            "unreadCount": 2
        }
    ]
}
```

#### ⚠️ 예외 상황

| 상황 | 응답코드 | 메시지 |
| --- | --- | --- |
| 액세스 토큰 권한 잘못됨 | 401 | 권한이 없습니다. |

#### 💻 클라이언트 사용 예시

**JavaScript (Fetch API)**
```javascript
async function getUnreadCount(accessToken) {
    try {
        const response = await fetch('http://localhost:8080/api/chat/rooms/unread-count', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('전체 읽지 않은 메시지:', data.totalUnreadCount);
        console.log('채팅방별 읽지 않은 메시지:', data.unreadCountByRoom);
        
        // 배지 업데이트 등 UI 작업
        updateUnreadBadge(data.totalUnreadCount);
        
        return data;
    } catch (error) {
        console.error('읽지 않은 메시지 개수 조회 실패:', error);
        throw error;
    }
}

// 주기적으로 읽지 않은 메시지 개수 확인 (예: 30초마다)
setInterval(() => {
    const token = localStorage.getItem('accessToken');
    getUnreadCount(token);
}, 30000);
```

---

## 5. 메시지 읽음 처리

> 특정 채팅방의 모든 읽지 않은 메시지를 읽음 처리합니다.

---

### 📌 Request

**Method:** `PUT`  
**URL:** `/api/chat/rooms/{chatRoomId}/messages/read`

#### Path Parameters

| 이름 | 설명 | 필수 |
| --- | --- | --- |
| chatRoomId | 채팅방 ID | ✅ |

#### Request Header

| 이름 | 설명 | 필수 |
| --- | --- | --- |
| Authorization | 액세스 토큰 기반 인증 (Bearer {ACCESS_TOKEN}) | ✅ |

---

### 📌 Response

#### Response Fields

| 이름 | 설명 | Type | 기타 |
| --- | --- | --- | --- |
| message | 처리 결과 메시지 | String |  |
| readCount | 읽음 처리된 메시지 개수 | Integer |  |

#### Response

```json
HTTP/1.1 200 Ok
Content-Type: application/json

{
    "message": "메시지 읽음 처리 완료",
    "readCount": 5
}
```

#### ⚠️ 예외 상황

| 상황 | 응답코드 | 메시지 |
| --- | --- | --- |
| 액세스 토큰 권한 잘못됨 | 401 | 권한이 없습니다. |
| 채팅방을 찾을 수 없음 | 404 | 채팅방을 찾을 수 없습니다. |
| 해당 채팅방에 접근할 수 없음 | 403 | 해당 채팅방에 접근할 수 없습니다. |

#### 💻 클라이언트 사용 예시

**JavaScript (Fetch API)**
```javascript
async function markMessagesAsRead(chatRoomId, accessToken) {
    try {
        const response = await fetch(`http://localhost:8080/api/chat/rooms/${chatRoomId}/messages/read`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log(`읽음 처리 완료: ${data.readCount}개의 메시지`);
        
        // UI 업데이트 (읽지 않은 메시지 표시 제거 등)
        updateReadStatus(chatRoomId);
        
        return data;
    } catch (error) {
        console.error('읽음 처리 실패:', error);
        throw error;
    }
}

// 채팅방 진입 시 자동으로 읽음 처리
function enterChatRoom(chatRoomId) {
    const token = localStorage.getItem('accessToken');
    markMessagesAsRead(chatRoomId, token);
}
```

---

## 6. 메시지 전송 (REST API)

> 특정 채팅방에 메시지를 전송합니다.  
> **주의:** 실제 구현에서는 WebSocket을 통해 전송하는 것을 권장합니다. 이 엔드포인트는 WebSocket을 사용할 수 없는 경우를 위한 대체 수단입니다.

---

### 📌 Request

**Method:** `POST`  
**URL:** `/api/chat/rooms/{chatRoomId}/messages`

#### Path Parameters

| 이름 | 설명 | 필수 |
| --- | --- | --- |
| chatRoomId | 채팅방 ID | ✅ |

#### Request Header

| 이름 | 설명 | 필수 |
| --- | --- | --- |
| Authorization | 액세스 토큰 기반 인증 (Bearer {ACCESS_TOKEN}) | ✅ |
| Content-Type | application/json | ✅ |

#### Request Body

| 이름 | 설명 | Type | 필수 | 제약조건 |
| --- | --- | --- | --- | --- |
| chatRoomId | 채팅방 ID | Long | ✅ | URL의 chatRoomId와 일치해야 함 |
| content | 메시지 내용 | String | ✅ | 최대 1000자, 공백 불가 |

#### Request Body Example

```json
{
    "chatRoomId": 1,
    "content": "안녕하세요"
}
```

---

### 📌 Response

#### Response Fields

| 이름 | 설명 | Type | 기타 |
| --- | --- | --- | --- |
| messageId | 메시지 ID | Long |  |
| chatRoomId | 채팅방 ID | Long |  |
| senderId | 발신자 ID | Long |  |
| senderName | 발신자 이름 | String |  |
| content | 메시지 원문 내용 | String |  |
| translatedContent | 메시지 번역 내용 | String | nullable (번역 중이거나 실패 시 null) |
| language | 메시지 언어 | String | KOREAN, JAPANESE |
| messageType | 메시지 타입 | String | TEXT, IMAGE, SYSTEM |
| isRead | 읽음 여부 | Boolean | 초기값: false |
| readAt | 읽은 시간 | String | nullable, ISO 8601 형식 |
| createdAt | 메시지 생성 시간 | String | ISO 8601 형식 |

#### Response

```json
HTTP/1.1 200 Ok
Content-Type: application/json

{
    "messageId": 11,
    "chatRoomId": 1,
    "senderId": 1,
    "senderName": "김태윤",
    "content": "안녕하세요",
    "translatedContent": null,
    "language": "KOREAN",
    "messageType": "TEXT",
    "isRead": false,
    "readAt": null,
    "createdAt": "2026-01-31T10:35:00"
}
```

**참고:** 메시지 전송 직후에는 `translatedContent`가 `null`일 수 있습니다. 번역은 비동기적으로 처리되며, 번역이 완료되면 WebSocket을 통해 업데이트된 메시지가 전송됩니다.

#### 💻 클라이언트 사용 예시

**JavaScript (Fetch API)**
```javascript
async function sendMessage(chatRoomId, content, accessToken) {
    try {
        // 입력 검증
        if (!content || content.trim().length === 0) {
            throw new Error('메시지 내용을 입력해주세요.');
        }
        
        if (content.length > 1000) {
            throw new Error('메시지는 최대 1000자까지 입력 가능합니다.');
        }
        
        const response = await fetch(`http://localhost:8080/api/chat/rooms/${chatRoomId}/messages`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                chatRoomId: chatRoomId,
                content: content.trim()
            })
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '메시지 전송 실패');
        }
        
        const data = await response.json();
        console.log('메시지 전송 완료:', data);
        
        // UI 업데이트 (메시지 목록에 추가)
        // 주의: translatedContent가 null일 수 있으므로 번역 완료를 기다려야 함
        addMessageToUI(data);
        
        return data;
    } catch (error) {
        console.error('메시지 전송 실패:', error);
        throw error;
    }
}

// 사용 예시
const token = localStorage.getItem('accessToken');
sendMessage(1, '안녕하세요', token);
```

**주의사항:**
- 이 REST API는 WebSocket을 사용할 수 없는 경우를 위한 대체 수단입니다.
- 실시간 채팅을 위해서는 WebSocket을 사용하는 것을 권장합니다.
- REST API로 전송한 메시지도 번역이 완료되면 WebSocket을 통해 업데이트됩니다.

#### ⚠️ 예외 상황

| 상황 | 응답코드 | 메시지 |
| --- | --- | --- |
| 액세스 토큰 권한 잘못됨 | 401 | 권한이 없습니다. |
| 채팅방을 찾을 수 없음 | 404 | 채팅방을 찾을 수 없습니다. |
| 해당 채팅방에 접근할 수 없음 | 403 | 해당 채팅방에 접근할 수 없습니다. |
| 요청 URL의 채팅방 ID와 요청 본문의 채팅방 ID가 일치하지 않음 | 400 | 요청 URL의 채팅방 ID와 요청 본문의 채팅방 ID가 일치하지 않습니다. |
| 메시지 내용이 올바르지 않음 | 400 | 메시지 내용이 올바르지 않습니다. |
| 메시지 전송 실패 | 500 | 메시지 전송에 실패했습니다. |
| 메시지 번역 실패 | 500 | 메시지 번역에 실패했습니다. 원문은 저장되었습니다. |

---

## 7. WebSocket 연결 설정

> 실시간 채팅을 위한 WebSocket 연결을 설정합니다.

---

### 📌 연결 방법

**프로토콜:** WebSocket (STOMP over WebSocket)  
**엔드포인트:** `ws://{host}/ws/chat?token={JWT_TOKEN}`  
**또는:** `wss://{host}/ws/chat?token={JWT_TOKEN}` (HTTPS 환경)

#### 연결 파라미터

| 이름 | 설명 | 필수 | 위치 |
| --- | --- | --- | --- |
| token | JWT 액세스 토큰 | ✅ | Query Parameter 또는 STOMP CONNECT 헤더 |

#### 인증 방법

**방법 1: Query Parameter (권장 - SockJS 호환)**
```
ws://localhost:8080/ws/chat?token={JWT_TOKEN}
```

**방법 2: STOMP CONNECT 헤더**
```
CONNECT
Authorization: Bearer {JWT_TOKEN}
```

#### 연결 프로세스

1. **WebSocket 핸드셰이크**
   - 클라이언트가 WebSocket 연결 요청
   - 서버가 JWT 토큰 검증
   - 인증 성공 시 연결 허용

2. **STOMP CONNECT 프레임**
   - 클라이언트가 STOMP CONNECT 프레임 전송
   - 서버가 토큰 재검증
   - 연결 완료

3. **구독 설정**
   - 클라이언트가 메시지 수신을 위한 구독 경로 설정

#### 💻 클라이언트 사용 예시

**JavaScript (SockJS + STOMP)**
```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

let stompClient = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;

function connectWebSocket(accessToken, userId, onMessage, onReadStatus) {
    // SockJS를 사용한 WebSocket 연결
    const socket = new SockJS(`http://localhost:8080/ws/chat?token=${accessToken}`);
    
    stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
            console.log('WebSocket 연결 성공:', frame);
            reconnectAttempts = 0;
            
            // 메시지 수신 구독
            stompClient.subscribe(`/user/${userId}/queue/messages`, (message) => {
                const messageData = JSON.parse(message.body);
                console.log('메시지 수신:', messageData);
                
                // 번역 완료 여부 확인
                if (messageData.translatedContent) {
                    console.log('번역 완료:', messageData.translatedContent);
                }
                
                onMessage(messageData);
            });
            
            // 읽음 상태 구독
            stompClient.subscribe(`/user/${userId}/queue/read-status`, (message) => {
                const readStatus = JSON.parse(message.body);
                console.log('읽음 상태 업데이트:', readStatus);
                onReadStatus(readStatus);
            });
        },
        onStompError: (frame) => {
            console.error('STOMP 오류:', frame);
            // 재연결 시도
            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++;
                setTimeout(() => {
                    connectWebSocket(accessToken, userId, onMessage, onReadStatus);
                }, 5000);
            }
        },
        onWebSocketClose: () => {
            console.log('WebSocket 연결 종료');
            // 자동 재연결
            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++;
                setTimeout(() => {
                    connectWebSocket(accessToken, userId, onMessage, onReadStatus);
                }, 5000);
            }
        }
    });
    
    stompClient.activate();
}

function disconnectWebSocket() {
    if (stompClient) {
        stompClient.deactivate();
        stompClient = null;
    }
}

// 사용 예시
const token = localStorage.getItem('accessToken');
const userId = getCurrentUserId(); // 현재 사용자 ID

connectWebSocket(
    token,
    userId,
    (message) => {
        // 메시지 수신 처리
        updateChatUI(message);
    },
    (readStatus) => {
        // 읽음 상태 업데이트 처리
        updateReadStatusUI(readStatus);
    }
);

// 앱 종료 시 연결 해제
window.addEventListener('beforeunload', () => {
    disconnectWebSocket();
});
```

**React Hook 예시**
```typescript
import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

interface Message {
    messageId: number;
    chatRoomId: number;
    senderId: number;
    content: string;
    translatedContent: string | null;
    language: string;
    createdAt: string;
}

function useWebSocket(accessToken: string, userId: number) {
    const [isConnected, setIsConnected] = useState(false);
    const [messages, setMessages] = useState<Message[]>([]);
    const stompClientRef = useRef<Client | null>(null);

    useEffect(() => {
        if (!accessToken || !userId) return;

        const socket = new SockJS(`http://localhost:8080/ws/chat?token=${accessToken}`);
        const client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            onConnect: () => {
                setIsConnected(true);
                
                // 메시지 구독
                client.subscribe(`/user/${userId}/queue/messages`, (message) => {
                    const messageData: Message = JSON.parse(message.body);
                    setMessages(prev => {
                        // 중복 방지
                        const exists = prev.find(m => m.messageId === messageData.messageId);
                        if (exists) {
                            // 번역 업데이트인 경우
                            return prev.map(m => 
                                m.messageId === messageData.messageId ? messageData : m
                            );
                        }
                        return [...prev, messageData];
                    });
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
            }
        });

        stompClientRef.current = client;
        client.activate();

        return () => {
            client.deactivate();
        };
    }, [accessToken, userId]);

    return { isConnected, messages, stompClient: stompClientRef.current };
}
```

#### ⚠️ 예외 상황

| 상황 | 응답코드 | 메시지 |
| --- | --- | --- |
| 토큰이 없음 | 401 | 권한이 없습니다. |
| 토큰 검증 실패 | 401 | 권한이 없습니다. |
| 토큰 만료 | 401 | 권한이 없습니다. |

---

## 8. 메시지 전송 (WebSocket)

> WebSocket을 통해 실시간으로 메시지를 전송합니다.

---

### 📌 Request

**Destination:** `/app/chat.send`  
**프로토콜:** STOMP

#### STOMP 메시지 형식

**Command:** `SEND`  
**Destination:** `/app/chat.send`  
**Headers:**
- `Authorization: Bearer {JWT_TOKEN}` (선택사항, 핸드셰이크에서 이미 인증됨)

**Body (JSON):**

| 이름 | 설명 | Type | 필수 | 제약조건 |
| --- | --- | --- | --- | --- |
| chatRoomId | 채팅방 ID | Long | ✅ |  |
| content | 메시지 내용 | String | ✅ | 최대 1000자, 공백 불가 |

#### Request Body Example

```json
{
    "chatRoomId": 1,
    "content": "안녕하세요"
}
```

---

### 📌 Response

#### 즉시 응답 (발신자에게)

**Destination:** `/user/{senderId}/queue/messages`

**Response Fields**

| 이름 | 설명 | Type | 기타 |
| --- | --- | --- | --- |
| messageId | 메시지 ID | Long |  |
| chatRoomId | 채팅방 ID | Long |  |
| senderId | 발신자 ID | Long |  |
| senderName | 발신자 이름 | String |  |
| content | 메시지 원문 내용 | String |  |
| translatedContent | 메시지 번역 내용 | String | nullable (번역 중이거나 실패 시 null) |
| language | 메시지 언어 | String | KOREAN, JAPANESE |
| messageType | 메시지 타입 | String | TEXT, IMAGE, SYSTEM |
| isRead | 읽음 여부 | Boolean | 초기값: false |
| readAt | 읽은 시간 | String | nullable, ISO 8601 형식 |
| createdAt | 메시지 생성 시간 | String | ISO 8601 형식 |

**Response Example**

```json
{
    "messageId": 11,
    "chatRoomId": 1,
    "senderId": 1,
    "senderName": "김태윤",
    "content": "안녕하세요",
    "translatedContent": null,
    "language": "KOREAN",
    "messageType": "TEXT",
    "isRead": false,
    "readAt": null,
    "createdAt": "2026-01-31T10:35:00"
}
```

#### 상대방에게 전송

**Destination:** `/user/{partnerId}/queue/messages`

상대방도 동일한 형식의 메시지를 수신합니다.

#### 번역 완료 후 업데이트

**Destination:** `/user/{userId}/queue/messages` (발신자 및 상대방 모두)

번역이 완료되면 `translatedContent` 필드가 포함된 업데이트된 메시지가 전송됩니다.

**Updated Response Example**

```json
{
    "messageId": 11,
    "chatRoomId": 1,
    "senderId": 1,
    "senderName": "김태윤",
    "content": "안녕하세요",
    "translatedContent": "こんにちは",
    "language": "KOREAN",
    "messageType": "TEXT",
    "isRead": false,
    "readAt": null,
    "createdAt": "2026-01-31T10:35:00"
}
```

**참고:** 
- 메시지 전송 직후에는 `translatedContent`가 `null`일 수 있습니다.
- 번역은 비동기적으로 처리되며, 완료되면 자동으로 업데이트된 메시지가 전송됩니다.
- 번역 완료 시간은 보통 100~300ms입니다 (캐시 히트 시 <10ms).

#### 💻 클라이언트 사용 예시

**JavaScript (STOMP 클라이언트)**
```javascript
function sendMessageViaWebSocket(chatRoomId, content, stompClient) {
    if (!stompClient || !stompClient.connected) {
        throw new Error('WebSocket이 연결되지 않았습니다.');
    }
    
    // 입력 검증
    if (!content || content.trim().length === 0) {
        throw new Error('메시지 내용을 입력해주세요.');
    }
    
    if (content.length > 1000) {
        throw new Error('메시지는 최대 1000자까지 입력 가능합니다.');
    }
    
    // 메시지 전송
    stompClient.publish({
        destination: '/app/chat.send',
        body: JSON.stringify({
            chatRoomId: chatRoomId,
            content: content.trim()
        })
    });
    
    console.log('메시지 전송 요청:', { chatRoomId, content });
}

// 사용 예시
const token = localStorage.getItem('accessToken');
const userId = getCurrentUserId();

connectWebSocket(token, userId, (message) => {
    // 메시지 수신 처리
    updateChatUI(message);
}, (readStatus) => {
    // 읽음 상태 처리
});

// 메시지 전송
sendMessageViaWebSocket(1, '안녕하세요', stompClient);
```

**React 컴포넌트 예시**
```typescript
import { useState } from 'react';

function ChatInput({ chatRoomId, stompClient }: { chatRoomId: number, stompClient: Client }) {
    const [message, setMessage] = useState('');
    const [sending, setSending] = useState(false);

    const handleSend = () => {
        if (!message.trim() || sending) return;
        
        setSending(true);
        
        try {
            stompClient.publish({
                destination: '/app/chat.send',
                body: JSON.stringify({
                    chatRoomId,
                    content: message.trim()
                })
            });
            
            setMessage('');
        } catch (error) {
            console.error('메시지 전송 실패:', error);
            alert('메시지 전송에 실패했습니다.');
        } finally {
            setSending(false);
        }
    };

    return (
        <div className="chat-input">
            <input
                type="text"
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSend()}
                maxLength={1000}
                placeholder="메시지를 입력하세요..."
            />
            <button onClick={handleSend} disabled={sending || !message.trim()}>
                {sending ? '전송 중...' : '전송'}
            </button>
        </div>
    );
}
```

**주의사항:**
- 메시지 전송 직후 응답에는 `translatedContent`가 `null`일 수 있습니다.
- 번역이 완료되면 `/user/{userId}/queue/messages` 경로로 업데이트된 메시지가 전송됩니다.
- 동일한 `messageId`의 메시지가 다시 수신되면 번역 완료 업데이트로 간주하고 UI를 업데이트해야 합니다.

#### ⚠️ 예외 상황

| 상황 | 응답코드 | 메시지 |
| --- | --- | --- |
| WebSocket 인증 실패 | 401 | WebSocket 인증에 실패했습니다. |
| 채팅방을 찾을 수 없음 | 404 | 채팅방을 찾을 수 없습니다. |
| 해당 채팅방에 접근할 수 없음 | 403 | 해당 채팅방에 접근할 수 없습니다. |
| 메시지 내용이 올바르지 않음 | 400 | 메시지 내용이 올바르지 않습니다. |
| 메시지 전송 실패 | 500 | 메시지 전송에 실패했습니다. |

---

## 9. 메시지 수신 (WebSocket 구독)

> 실시간으로 수신되는 메시지를 구독합니다.

---

### 📌 구독 설정

**Subscribe Destination:** `/user/{userId}/queue/messages`

**설명:**
- `{userId}`는 현재 로그인한 사용자의 회원 ID입니다.
- 이 경로를 구독하면 본인에게 전송된 모든 메시지를 실시간으로 수신할 수 있습니다.

#### 구독 예시 (JavaScript)

```javascript
// STOMP 클라이언트 생성
const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/ws/chat?token=' + jwtToken,
    connectHeaders: {
        Authorization: 'Bearer ' + jwtToken
    }
});

// 연결 후 구독
stompClient.onConnect = function(frame) {
    const userId = getCurrentUserId(); // 현재 사용자 ID
    
    // 메시지 수신 구독
    stompClient.subscribe('/user/' + userId + '/queue/messages', function(message) {
        const messageData = JSON.parse(message.body);
        console.log('메시지 수신:', messageData);
        // UI 업데이트 로직
    });
};
```

#### 수신 메시지 형식

수신되는 메시지는 [메시지 전송 (WebSocket)](#8-메시지-전송-websocket)의 Response 형식과 동일합니다.

#### 💻 클라이언트 사용 예시

**메시지 수신 처리 로직**
```javascript
// 메시지 수신 구독 및 처리
stompClient.subscribe(`/user/${userId}/queue/messages`, (message) => {
    const messageData = JSON.parse(message.body);
    
    // 메시지 ID로 중복 확인
    const existingMessage = findMessageById(messageData.messageId);
    
    if (existingMessage) {
        // 번역 완료 업데이트인 경우
        if (messageData.translatedContent && !existingMessage.translatedContent) {
            console.log('번역 완료:', messageData.translatedContent);
            updateMessageTranslation(messageData);
        }
    } else {
        // 새로운 메시지인 경우
        console.log('새 메시지 수신:', messageData);
        addNewMessage(messageData);
    }
    
    // UI 업데이트
    updateChatUI(messageData);
});

// 메시지 번역 상태 표시
function displayMessage(message) {
    if (message.translatedContent) {
        // 번역 완료: 원문과 번역문 모두 표시
        return {
            original: message.content,
            translated: message.translatedContent,
            language: message.language
        };
    } else {
        // 번역 중: 원문만 표시하고 "번역 중..." 표시
        return {
            original: message.content,
            translated: null,
            isTranslating: true
        };
    }
}
```

---

## 10. 읽음 상태 업데이트 (WebSocket)

> WebSocket을 통해 읽음 상태를 실시간으로 업데이트합니다.

---

### 📌 Request

**Destination:** `/app/chat.read`  
**프로토콜:** STOMP

#### STOMP 메시지 형식

**Command:** `SEND`  
**Destination:** `/app/chat.read`

**Body (JSON):**

| 이름 | 설명 | Type | 필수 |
| --- | --- | --- | --- |
| chatRoomId | 채팅방 ID | Long | ✅ |

#### Request Body Example

```json
{
    "chatRoomId": 1
}
```

---

### 📌 Response

#### 상대방에게 읽음 상태 알림 전송

**Destination:** `/user/{partnerId}/queue/read-status`

**Response Fields**

| 이름 | 설명 | Type | 기타 |
| --- | --- | --- | --- |
| chatRoomId | 채팅방 ID | Long |  |
| readCount | 읽음 처리된 메시지 개수 | Integer |  |

**Response Example**

```json
{
    "chatRoomId": 1,
    "readCount": 5
}
```

**설명:**
- 특정 채팅방의 모든 읽지 않은 메시지가 읽음 처리됩니다.
- 상대방에게 읽음 상태 알림이 자동으로 전송됩니다.

#### 💻 클라이언트 사용 예시

**JavaScript (STOMP 클라이언트)**
```javascript
function markAsReadViaWebSocket(chatRoomId, stompClient) {
    if (!stompClient || !stompClient.connected) {
        throw new Error('WebSocket이 연결되지 않았습니다.');
    }
    
    // 읽음 처리 요청 전송
    stompClient.publish({
        destination: '/app/chat.read',
        body: JSON.stringify({
            chatRoomId: chatRoomId
        })
    });
    
    console.log('읽음 처리 요청:', chatRoomId);
}

// 채팅방 진입 시 자동으로 읽음 처리
function enterChatRoom(chatRoomId) {
    markAsReadViaWebSocket(chatRoomId, stompClient);
    
    // UI 업데이트 (읽지 않은 메시지 표시 제거)
    updateUnreadMessages(chatRoomId);
}
```

**React Hook 예시**
```typescript
function useMarkAsRead(chatRoomId: number, stompClient: Client | null) {
    const markAsRead = () => {
        if (!stompClient?.connected) {
            console.warn('WebSocket이 연결되지 않았습니다.');
            return;
        }
        
        stompClient.publish({
            destination: '/app/chat.read',
            body: JSON.stringify({ chatRoomId })
        });
    };
    
    return { markAsRead };
}

// 사용 예시
function ChatRoom({ chatRoomId }: { chatRoomId: number }) {
    const { markAsRead } = useMarkAsRead(chatRoomId, stompClient);
    
    useEffect(() => {
        // 채팅방 진입 시 읽음 처리
        markAsRead();
    }, [chatRoomId]);
    
    return <div>...</div>;
}
```

#### ⚠️ 예외 상황

| 상황 | 응답코드 | 메시지 |
| --- | --- | --- |
| WebSocket 인증 실패 | 401 | WebSocket 인증에 실패했습니다. |
| 채팅방을 찾을 수 없음 | 404 | 채팅방을 찾을 수 없습니다. |
| 해당 채팅방에 접근할 수 없음 | 403 | 해당 채팅방에 접근할 수 없습니다. |

---

## 11. 읽음 상태 수신 (WebSocket 구독)

> 상대방의 읽음 상태를 실시간으로 수신합니다.

---

### 📌 구독 설정

**Subscribe Destination:** `/user/{userId}/queue/read-status`

**설명:**
- `{userId}`는 현재 로그인한 사용자의 회원 ID입니다.
- 이 경로를 구독하면 상대방이 메시지를 읽었을 때 알림을 받을 수 있습니다.

#### 구독 예시 (JavaScript)

```javascript
stompClient.subscribe('/user/' + userId + '/queue/read-status', function(message) {
    const readStatus = JSON.parse(message.body);
    console.log('읽음 상태 업데이트:', readStatus);
    // UI 업데이트 로직 (읽음 표시 등)
});
```

#### 수신 메시지 형식

수신되는 메시지는 [읽음 상태 업데이트 (WebSocket)](#10-읽음-상태-업데이트-websocket)의 Response 형식과 동일합니다.

#### 💻 클라이언트 사용 예시

**읽음 상태 수신 처리**
```javascript
// 읽음 상태 구독
stompClient.subscribe(`/user/${userId}/queue/read-status`, (message) => {
    const readStatus = JSON.parse(message.body);
    console.log('읽음 상태 업데이트:', readStatus);
    
    // 해당 채팅방의 메시지 읽음 표시 업데이트
    updateReadStatus(readStatus.chatRoomId, readStatus.readCount);
    
    // UI 업데이트 (읽음 표시 아이콘 등)
    showReadReceipt(readStatus.chatRoomId);
});

// 읽음 상태 UI 업데이트 함수
function updateReadStatus(chatRoomId, readCount) {
    // 해당 채팅방의 메시지들에 읽음 표시 추가
    const messages = getMessagesByChatRoom(chatRoomId);
    const unreadMessages = messages.filter(m => !m.isRead);
    
    // 읽음 처리된 메시지 개수만큼 읽음 표시
    unreadMessages.slice(0, readCount).forEach(message => {
        message.isRead = true;
        message.readAt = new Date().toISOString();
    });
    
    // UI 업데이트
    renderMessages(chatRoomId);
}
```

---

## WebSocket 공통 사항

### STOMP Destination 규칙

| Destination Prefix | 설명 | 예시 |
| --- | --- | --- |
| `/app` | 클라이언트가 서버로 메시지를 보낼 때 사용 | `/app/chat.send`, `/app/chat.read` |
| `/user/{userId}/queue` | 특정 사용자에게 메시지를 보낼 때 사용 | `/user/1/queue/messages` |
| `/queue` | 일반 큐 (브로커에서 사용) | - |
| `/topic` | 토픽 (브로커에서 사용) | - |

### 메시지 흐름

1. **메시지 전송**
   - 클라이언트 → `/app/chat.send` → 서버
   - 서버 → `/user/{senderId}/queue/messages` → 발신자
   - 서버 → `/user/{partnerId}/queue/messages` → 상대방

2. **번역 완료 업데이트**
   - 서버 → `/user/{senderId}/queue/messages` → 발신자 (번역문 포함)
   - 서버 → `/user/{partnerId}/queue/messages` → 상대방 (번역문 포함)

3. **읽음 상태 업데이트**
   - 클라이언트 → `/app/chat.read` → 서버
   - 서버 → `/user/{partnerId}/queue/read-status` → 상대방

### 연결 유지

- WebSocket 연결은 지속적으로 유지되어야 합니다.
- 연결이 끊어지면 자동 재연결 로직을 구현하는 것을 권장합니다.
- 토큰 만료 시 재인증 후 재연결해야 합니다.

### SockJS 지원

- 서버는 SockJS를 지원합니다.
- WebSocket을 지원하지 않는 환경에서도 폴백 옵션으로 동작합니다.
- 클라이언트는 SockJS 라이브러리를 사용하여 연결할 수 있습니다.

### 💻 전체 통합 예시 (React)

```typescript
import { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

interface ChatRoom {
    chatRoomId: number;
    partner: { name: string };
    unreadCount: number;
}

function ChatApp() {
    const [chatRooms, setChatRooms] = useState<ChatRoom[]>([]);
    const [currentRoom, setCurrentRoom] = useState<number | null>(null);
    const [messages, setMessages] = useState([]);
    const [stompClient, setStompClient] = useState<Client | null>(null);
    const accessToken = localStorage.getItem('accessToken');
    const userId = getCurrentUserId();

    // WebSocket 연결
    useEffect(() => {
        if (!accessToken || !userId) return;

        const socket = new SockJS(`http://localhost:8080/ws/chat?token=${accessToken}`);
        const client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            onConnect: () => {
                // 메시지 수신 구독
                client.subscribe(`/user/${userId}/queue/messages`, (message) => {
                    const messageData = JSON.parse(message.body);
                    setMessages(prev => {
                        const exists = prev.find(m => m.messageId === messageData.messageId);
                        return exists 
                            ? prev.map(m => m.messageId === messageData.messageId ? messageData : m)
                            : [...prev, messageData];
                    });
                });

                // 읽음 상태 구독
                client.subscribe(`/user/${userId}/queue/read-status`, (message) => {
                    const readStatus = JSON.parse(message.body);
                    // 읽음 상태 UI 업데이트
                    updateReadStatusUI(readStatus);
                });
            }
        });

        client.activate();
        setStompClient(client);

        return () => {
            client.deactivate();
        };
    }, [accessToken, userId]);

    // 채팅방 목록 조회
    useEffect(() => {
        fetch('/api/chat/rooms', {
            headers: { 'Authorization': `Bearer ${accessToken}` }
        })
        .then(res => res.json())
        .then(data => setChatRooms(data.chatRooms));
    }, []);

    // 메시지 전송
    const sendMessage = (chatRoomId: number, content: string) => {
        if (!stompClient?.connected) return;
        
        stompClient.publish({
            destination: '/app/chat.send',
            body: JSON.stringify({ chatRoomId, content })
        });
    };

    // 읽음 처리
    const markAsRead = (chatRoomId: number) => {
        if (!stompClient?.connected) return;
        
        stompClient.publish({
            destination: '/app/chat.read',
            body: JSON.stringify({ chatRoomId })
        });
    };

    return (
        <div className="chat-app">
            {/* 채팅방 목록 */}
            <div className="chat-room-list">
                {chatRooms.map(room => (
                    <div 
                        key={room.chatRoomId}
                        onClick={() => {
                            setCurrentRoom(room.chatRoomId);
                            markAsRead(room.chatRoomId);
                        }}
                    >
                        {room.partner.name}
                        {room.unreadCount > 0 && (
                            <span className="unread-badge">{room.unreadCount}</span>
                        )}
                    </div>
                ))}
            </div>

            {/* 채팅 화면 */}
            {currentRoom && (
                <ChatWindow 
                    chatRoomId={currentRoom}
                    messages={messages.filter(m => m.chatRoomId === currentRoom)}
                    onSendMessage={(content) => sendMessage(currentRoom, content)}
                />
            )}
        </div>
    );
}
```

---

## 공통 사항

### ⚠️ WebSocket API 명세 추가 지연에 대한 해명

**작성일:** 2026-01-31

본 API 명세서 작성 시 WebSocket 통신 명세가 누락된 이유는 다음과 같습니다:

1. **초기 요구사항의 범위**
   - 초기 요청 시 "채팅관련 API 명세 모두 작성해줘"라고 하셨으며, 일반적으로 "API 명세"라고 하면 REST API를 의미하는 경우가 많아 REST API 위주로 작성했습니다.
   - WebSocket은 프로토콜 특성상 "API"보다는 "통신 프로토콜" 또는 "메시징"으로 분류되는 경우가 많아 별도 섹션으로 분리해야 할 필요성을 인지하지 못했습니다.

2. **구현 우선순위**
   - 채팅 기능 구현 과정에서 REST API가 먼저 완성되었고, WebSocket은 실시간 통신을 위한 보완 기능으로 구현되었습니다.
   - REST API 명세 작성 시점에는 WebSocket 구현이 완료되지 않았거나, 완료되었더라도 명세화의 필요성을 간과했습니다.

3. **문서화 누락**
   - WebSocket 구현은 완료되었으나, STOMP 프로토콜의 Destination과 메시지 형식에 대한 명세를 문서화하지 않았습니다.
   - REST API와 달리 WebSocket은 요청-응답 구조가 명확하지 않고, 구독 기반의 양방향 통신이므로 명세 작성이 더 복잡하다는 점을 고려하지 못했습니다.

4. **사용자 피드백 부재**
   - WebSocket 명세가 누락된 점에 대한 피드백이 없어 추가 작업이 필요하다는 것을 인지하지 못했습니다.

**개선 조치:**
- 본 문서에 WebSocket 통신 명세를 추가하여 REST API와 WebSocket 모두를 포함한 완전한 API 명세서로 업데이트했습니다.
- 향후 API 명세서 작성 시 REST API뿐만 아니라 WebSocket, GraphQL 등 모든 통신 방식을 포함하도록 하겠습니다.

**추가된 내용:**
- WebSocket 연결 설정 및 인증 방법
- 메시지 전송/수신 (WebSocket)
- 읽음 상태 업데이트/수신 (WebSocket)
- STOMP Destination 규칙 및 메시지 흐름
- 구독 예시 코드

---

### 인증 방식

모든 채팅 API는 JWT(JSON Web Token) 기반 인증을 사용합니다.  
요청 헤더에 `Authorization: Bearer {ACCESS_TOKEN}` 형식으로 토큰을 포함해야 합니다.

### 언어 및 번역

- 지원 언어: 한국어(KOREAN), 일본어(JAPANESE)
- 메시지는 자동으로 감지되어 상대방의 언어로 번역됩니다.
- 번역은 비동기적으로 처리되며, 초기 응답에는 원문만 포함될 수 있습니다.
- 번역이 완료되면 WebSocket을 통해 업데이트된 메시지가 전송됩니다.

### 메시지 타입

- `TEXT`: 일반 텍스트 메시지
- `IMAGE`: 이미지 메시지 (향후 확장)
- `SYSTEM`: 시스템 메시지

### 시간 형식

모든 시간 필드는 ISO 8601 형식(`yyyy-MM-ddTHH:mm:ss`)을 사용합니다.

### 페이징

메시지 목록 조회 API는 Spring Data의 페이징 기능을 사용합니다.
- 페이지 번호는 0부터 시작합니다.
- 기본 페이지 크기는 20입니다.
- 정렬은 기본적으로 생성 시간 내림차순(최신순)입니다.

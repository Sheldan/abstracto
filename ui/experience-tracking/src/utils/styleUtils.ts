export default function createStyle(obj: any) {
    if(obj == null || obj.r == null) {
        return ''
    }
    return  `#${obj.r.toString(16).padStart(2, '0')}${obj.g.toString(16).padStart(2, '0')}${obj.b.toString(16).padStart(2, '0')}`
}